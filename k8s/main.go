package main

import (
	"flag"
	"fmt"
	"log"
	"net/url"
	"os"
	"path/filepath"
	"strings"

	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"

	"k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/runtime/schema"
	"k8s.io/client-go/discovery"
	"k8s.io/client-go/dynamic"
	"k8s.io/client-go/rest"
	"k8s.io/client-go/tools/clientcmd"
)

func main() {
	var groups, kinds, ns, version, names, label, field string
	kubeconfig := os.Getenv("KUBECONFIG")
	flag.StringVar(&groups, "groups", "", "comma-sep list of API groups")
	flag.StringVar(&kinds, "kinds", "", "comma-sep list of API kinds")
	flag.StringVar(&ns, "namespace", "", "namespace for resource")
	flag.StringVar(&version, "version", "", "resource version")
	flag.StringVar(&names, "names", "", "object names")
	flag.StringVar(&label, "l", "", "Label selector")
	flag.StringVar(&field, "f", "", "Field selector")
	flag.StringVar(&kubeconfig, "kubeconfig", kubeconfig, "kubeconfig file")
	flag.Parse()

	// validation
	if groups == "" {
		log.Fatal("--group is required")
	}

	// bootstrap config
	if kubeconfig == "" {
		kubeconfig = filepath.Join(os.Getenv("HOME"), ".kube", "config")
	}
	fmt.Println()
	fmt.Println("Using kubeconfig: ", kubeconfig)
	config, err := clientcmd.BuildConfigFromFlags("", kubeconfig)
	if err != nil {
		panic(err.Error())
	}

	disco, err := discovery.NewDiscoveryClientForConfig(config)
	if err != nil {
		log.Fatal(err)
	}

	k8sc, err := dynamic.NewForConfig(config)
	if err != nil {
		log.Fatal(err)
	}

	// restc, err := rest.RESTClientFor(config)
	// if err != nil {
	// 	log.Fatalf("ERR: initializing RESTClient: %s", err)
	// }

	grpList, err := disco.ServerGroups()
	if err != nil {
		log.Fatal(err)
	}

	for _, grp := range grpList.Groups {
		// filter by group
		grpName := strings.TrimSpace(grp.Name)
		grpName = getLegacyGrpName(grpName)
		if len(groups) > 0 && !strings.Contains(groups, grpName) {
			continue
		}
		// filter by group version
		for _, discoGV := range grp.Versions {
			if len(version) > 0 && discoGV.Version != version {
				//log.Printf("WARN: Version %s not matched for %s", version, grpName)
				continue
			}

			// adjust version for legacy group
			grpVersion := discoGV.GroupVersion
			if grpName == "core" {
				grpVersion = discoGV.Version
			}

			resources, err := disco.ServerResourcesForGroupVersion(grpVersion)
			if err != nil {
				//log.Printf("ERR: failed to get resource for %s: %s", grpVersion, err)
				continue
			}

			// filter by resource kind
			for _, res := range resources.APIResources {
				resKinds := strings.Split(kinds, ",")
				if len(kinds) > 0 && !isResOfKind(res, resKinds...) {
					//log.Printf("WARN: Kinds %s not matched for %s", kinds, res.Name)
					continue
				}

				gvr := schema.GroupVersionResource{
					Group:    toLegacyGrpName(grpName),
					Version:  discoGV.Version,
					Resource: res.Name,
				}

				var unstructList *unstructured.UnstructuredList
				if len(ns) > 0 && res.Namespaced {
					unstructList, err = k8sc.Resource(gvr).Namespace(ns).List(metav1.ListOptions{})
				} else {
					unstructList, err = k8sc.Resource(gvr).List(metav1.ListOptions{})
				}

				if err != nil {
					//log.Printf("WARN: failed to get list for res %s: %s", gvr, err)
					continue
				}

				for _, unstruct := range unstructList.Items {
					fmt.Printf("%s/%s/%s: %s\n", grpName, discoGV.Version, res.Name, unstruct.GetName())
				}

			}

		}
	}
}

func toLegacyGrpName(str string) string {
	if str == "core" {
		return ""
	}
	return str
}

func getLegacyGrpName(str string) string {
	if str == "" {
		return "core"
	}
	return str
}

func isGrpMatched(apiGrp metav1.APIGroup, selGrps ...string) bool {
	if len(selGrps) == 0 {
		return false
	}
	for _, selGrp := range selGrps {
		if apiGrp.Name == strings.TrimSpace(selGrp) {
			return true
		}
	}
	return false
}

func isResOfKind(res metav1.APIResource, kinds ...string) bool {
	if len(kinds) == 0 {
		return false
	}
	for k := range kinds {
		kind := strings.TrimSpace(kinds[k])
		switch {
		case strings.HasPrefix(strings.ToLower(res.Kind), strings.ToLower(kind)),
			strings.HasPrefix(strings.ToLower(res.Name), strings.ToLower(kind)):
			return true
		}
	}
	return false
}

func isResOfVer(res metav1.APIResource, ver string) bool {
	if ver == "" {
		return false
	}
	return ver == res.Version
}

func discoResourcesByGroupVersion(restc rest.Interface, gv metav1.GroupVersionForDiscovery) ([]metav1.APIResource, error) {
	var url url.URL
	if gv.String() == "" {
		return nil, fmt.Errorf("group and version are empty")
	}
	groupVer := gv.String()
	if groupVer == "v1" {
		url.Path = fmt.Sprintf("/api/%s", groupVer)
	} else {
		url.Path = "/apis/" + groupVer
	}
	resList := &metav1.APIResourceList{
		GroupVersion: groupVer,
	}
	err := restc.Get().AbsPath(url.String()).Do().Into(resList)
	if err != nil {
		// handle legacy groupVersion if 403 or 404.
		if groupVer == "v1" && (!errors.IsNotFound(err) && !errors.IsForbidden(err)) {
			return nil, err
		}
	}

	return resList.APIResources, nil
}
