(ns smt-onto.crawl
  (:require [clojure.string :as cstr]
            [net.cgrand.enlive-html :as html]))

(def wiki-root "http://megamitensei.wikia.com/wiki")
(def skills-list-page (str wiki-root "/List_of_Shin_Megami_Tensei_IV_Skills"))
(def attack-skill-types
  #{"Gun skills" "Electric skills" "Physical skills"
    "Fire skills" "Force skills" "Ice skills"})

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

(defn attack-skills
  ([] (attack-skills skills-list-page attack-skill-types))
  ([url attack-skill-types]
     (let [html-map (skills-html url)]
       (mapcat
        (fn [skill-type]
          (let [raw-data (-> (html-map skill-type)
                             (html/select [:tr])
                             ;; First row is headings
                             rest
                             (html/select [:td]))
                raw-skills (partition 8 raw-data)

                parse-skill
                (fn [raw-skill]

                  (let [[name rank mp damage hits target remark _]
                        (map (comp sanitize-text html/text) raw-skill)]
                    {:name name :rank rank :mp mp :damage damage
                     :hits hits :target target :remark remark
                     :element (first (cstr/split skill-type #" "))}))]
            (doall (map parse-skill raw-skills))))
        attack-skill-types))))
