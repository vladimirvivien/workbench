package main

import (
	"flag"
	"fmt"
	"log"
	"os"
	"path/filepath"
	"strings"

	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"

	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/runtime/schema"
	"k8s.io/client-go/discovery"
	"k8s.io/client-go/dynamic"
	"k8s.io/client-go/tools/clientcmd"
)

func main() {
	var groups, kinds, ns, version, names, labels, fields string
	kubeconfig := os.Getenv("KUBECONFIG")
	flag.StringVar(&groups, "groups", "", "comma-sep list of API groups")
	flag.StringVar(&kinds, "kinds", "", "comma-sep list of API kinds")
	flag.StringVar(&ns, "namespace", "", "namespace for resource")
	flag.StringVar(&version, "version", "", "resource version")
	flag.StringVar(&names, "names", "", "object names")
	flag.StringVar(&labels, "l", "", "Label selector")
	flag.StringVar(&fields, "f", "", "Field selector")
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
				if len(kinds) > 0 && !strings.Contains(strings.ToLower(kinds), strings.ToLower(res.Kind)) {
					//log.Printf("WARN: Kinds %s not matched for %s", res.Kind, res.Name)
					continue
				}

				gvr := schema.GroupVersionResource{
					Group:    toLegacyGrpName(grpName),
					Version:  discoGV.Version,
					Resource: res.Name,
				}

				// select by namespace if any
				listOptions := metav1.ListOptions{
					LabelSelector: labels,
					FieldSelector: fields,
				}
				var unstructList *unstructured.UnstructuredList
				if len(ns) > 0 && res.Namespaced {
					unstructList, err = k8sc.Resource(gvr).Namespace(ns).List(listOptions)
				} else {
					unstructList, err = k8sc.Resource(gvr).List(listOptions)
				}

				if err != nil {
					//log.Printf("WARN: failed to get list for res %s: %s", gvr, err)
					continue
				}

				// filter based on names
				for _, unstruct := range unstructList.Items {
					if len(names) > 0 && !strings.Contains(names, unstruct.GetName()) {
						continue
					}
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
