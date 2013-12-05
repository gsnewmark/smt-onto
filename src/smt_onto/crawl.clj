(ns smt-onto.crawl
  (:require [clojure.string :as cstr]
            [net.cgrand.enlive-html :as html]))

(def wiki-root "http://megamitensei.wikia.com/wiki")
(def skills-list-page (str wiki-root "/List_of_Shin_Megami_Tensei_IV_Skills"))
(def attack-skill-types
  #{"Gun skills" "Electric skills" "Physical skills"
    "Fire skills" "Force skills" "Ice skills"})
(def instant-kill-skill-types #{"Light skills" "Dark skills"})

(defn- sanitize-text
  [text]
  (or (first (re-seq #"[A-Za-z0-9 %,~_\-]+" text)) ""))

(defn- fetch-url [url] (html/html-resource (java.net.URL. url)))

(defn- skills-html [url]
  (some->> (html/select (fetch-url url)
                    #{[:span.mw-headline] [:table.smt4]})
           (partition 2)
           (map (fn [[k v]] [(html/text k) v]))
           (into {})))

(defn- parse-skills
  [url skill-types extract-skills parse-skill]
  (let [html-map (skills-html url)]
    (mapcat
     (fn [skill-type]
       (let [raw-data (-> (html-map skill-type)
                          (html/select [:tr])
                          ;; First row is headings
                          rest
                          (html/select [:td]))
             raw-skills (extract-skills raw-data)]
         (doall (map (partial parse-skill skill-type) raw-skills))))
     skill-types)))

(defn- get-type [skill-type]
  (first (cstr/split skill-type #" ")))

(defn attack-skills
  ([] (attack-skills skills-list-page attack-skill-types))
  ([url skill-types]
     (parse-skills url skill-types
                   (partial partition 8)
                   (fn [skill-type raw-skill]
                     (let [[name rank mp damage hits target remark _]
                           (map (comp sanitize-text html/text) raw-skill)]
                       {:name name :rank rank :mp mp :damage damage
                        :hits hits :target target :remark remark
                        :element (get-type skill-type)})))))

(defn instant-kill-skills
  ([] (instant-kill-skills skills-list-page instant-kill-skill-types))
  ([url skill-types]
     (parse-skills url skill-types
                   (partial partition 6)
                   (fn [skill-type raw-skill]
                     (let [[name rank mp fatal-chance target _]
                           (map (comp sanitize-text html/text) raw-skill)]
                       {:name name :rank rank :mp mp :target target
                        :fatal-chance
                        (double (/ (Integer/parseInt (first (re-seq #"\d+"
                                                                    fatal-chance)))
                                   100))
                        :alignment-type (get-type skill-type)})))))
