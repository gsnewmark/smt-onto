(ns smt-onto.core
  (:require [smt-onto.ontology :as so]
            [tawny.owl :refer [save-ontology]])
  (:gen-class))

(defn export-onto [] (save-ontology so/smt-ontology "resources/smt.owl" :owl))

(defn -main [& args]
  (export-onto))
