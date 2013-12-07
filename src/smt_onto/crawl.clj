(ns smt-onto.crawl
  (:require [clojure.string :as cstr]
            [net.cgrand.enlive-html :as html]))

(def wiki-root "http://megamitensei.wikia.com/wiki")
(def skills-list-page (str wiki-root "/List_of_Shin_Megami_Tensei_IV_Skills"))
(def skill-types
  {:attack #{"Gun skills" "Electric skills" "Physical skills"
             "Fire skills" "Force skills" "Ice skills" "Almighty skills"}
   :instant-kill #{"Light skills" "Dark skills"}
   :ailment #{"Ailment skills"}
   :support #{"Support skills"}})

(defn- fetch-url [url] (html/html-resource (java.net.URL. url)))

(defn- skills-html [url]
  (some->> (html/select (fetch-url url)
                    #{[:span.mw-headline] [:table.smt4]})
           (partition 2)
           (map (fn [[k v]] [(html/text k) v]))
           (into {})))

(defn- sanitize-text
  [text]
  (or (first (re-seq #"[A-Za-z0-9 %,~_\-.\"']+" text)) ""))

(defn- get-type [skill-type]
  (first (cstr/split skill-type #" ")))

(defn- percents->double
  "Transform string with percents (\"30%\") to double [0.0, 1.0]."
  [s]
  (double (/ (Integer/parseInt (first (re-seq #"\d+" s))) 100)))

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
             ;; Some tables have rows with variable-length columns
             raw-data (mapcat
                       (fn [r]
                         (if-let [n (get-in r [:attrs :colspan])]
                           (concat [r] (repeat (dec (Integer/parseInt n)) {}))
                           [r]))
                       raw-data)
             raw-skills (extract-skills raw-data)]
         (doall (map (partial parse-skill skill-type) raw-skills))))
     skill-types)))

(defn attack-skills
  ([] (attack-skills skills-list-page (:attack skill-types)))
  ([url skill-types]
     (parse-skills url skill-types
                   (partial partition 8)
                   (fn [skill-type raw-skill]
                     (let [[name rank mp damage hits target remark _]
                           (map (comp sanitize-text html/text) raw-skill)]
                       {:name name :rank rank :damage damage
                        :hits hits :target target :remark remark
                        :mp (Integer/parseInt mp)
                        :element (get-type skill-type)})))))

(defn instant-kill-skills
  ([] (instant-kill-skills skills-list-page (:instant-kill skill-types)))
  ([url skill-types]
     (parse-skills url skill-types
                   (partial partition 6)
                   (fn [skill-type raw-skill]
                     (let [[name rank mp fatal-chance target _]
                           (map (comp sanitize-text html/text) raw-skill)]
                       {:name name :rank rank :target target
                        :mp (Integer/parseInt mp)
                        :fatal-chance (percents->double fatal-chance)
                        :alignment-type (get-type skill-type)})))))

(defn ailment-skills
  ([] (ailment-skills skills-list-page (:ailment skill-types)))
  ([url skill-types]
     (parse-skills url skill-types
                   (partial partition 8)
                   (fn [skill-type raw-skill]
                     (let [[name rank mp target chance ailment remark _]
                           (map (comp sanitize-text html/text) raw-skill)]
                       {:name name :rank rank :mp (Integer/parseInt mp)
                        :target target :ailment ailment :remark remark
                        :chance (percents->double chance)})))))

(defn support-skills
  ([] (support-skills skills-list-page (:support skill-types)))
  ([url skill-types]
     (parse-skills url skill-types
                   (partial partition 6)
                   (fn [skill-type raw-skill]
                     (let [[name rank mp target effect  _]
                           (map (comp sanitize-text html/text) raw-skill)]
                       {:name name :rank rank :mp (Integer/parseInt mp)
                        :target target :effect effect})))))
