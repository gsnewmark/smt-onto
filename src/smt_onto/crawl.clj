(ns smt-onto.crawl
  (:require [clojure.string :as cstr]
            [net.cgrand.enlive-html :as html]))

(def wiki-root "http://megamitensei.wikia.com")
(def skills-list-page
  (str wiki-root "/wiki/List_of_Shin_Megami_Tensei_IV_Skills"))
(def demons-list-page
  (str wiki-root "/wiki/List_of_Shin_Megami_Tensei_IV_Demons"))
(def skill-types
  {:attack #{"Gun skills" "Electric skills" "Physical skills"
             "Fire skills" "Force skills" "Ice skills" "Almighty skills"}
   :instant-kill #{"Light skills" "Dark skills"}
   :ailment #{"Ailment skills"}
   :support #{"Support skills"}
   :stat-modifier #{"Stat modifiers"}
   :healing #{"Healing skills"}
   :auto #{"Auto skills"}})

(defn- fetch-url [url] (html/html-resource (java.net.URL. url)))

(defn- skills-html [url]
  (some->> (html/select (fetch-url url)
                    #{[:span.mw-headline] [:table.smt4]})
           (partition 2)
           (map (fn [[k v]] [(html/text k) v]))
           (into {})))

(defn- demons-list-html [url]
  (html/select (fetch-url url) #{[:table.smt4 :a]}))

;;; TODO some demons have 2 tables (for boss version and ally version):
;;;      http://megamitensei.wikia.com/wiki/Murmur
;;;      http://megamitensei.wikia.com/wiki/Raphael
(defn- demon-html [url]
  (remove string?
   (html/select (fetch-url url)
                #{[:table.smt4] [(html/rights [:table.smt4])]})))

(defn- sanitize-text
  [text]
  (or (first (re-seq #"[A-Za-z0-9 %,~_\-.\"':]+" text)) ""))

(defn- get-type [skill-type]
  (first (cstr/split skill-type #" ")))

(defn- percents->double
  "Transform string with percents (\"30%\") to double [0.0, 1.0]."
  [s]
  (double (/ (Integer/parseInt (first (re-seq #"\d+" s))) 100)))

(defn- html->text [html] ((comp cstr/trim sanitize-text html/text) html))

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
             raw-skills (extract-skills raw-data)
             skills (map #(map html->text %) raw-skills)]
         (doall (map (partial parse-skill skill-type) skills))))
     skill-types)))

(defn attack-skills
  ([] (attack-skills skills-list-page (:attack skill-types)))
  ([url skill-types]
     (parse-skills url skill-types
                   (partial partition 8)
                   (fn [skill-type skill]
                     (let [[name rank mp damage hits target remark _] skill]
                       {:name name :rank rank :damage damage
                        :hits hits :target target :remark remark
                        :mp (Integer/parseInt mp)
                        :element (get-type skill-type)})))))

(defn instant-kill-skills
  ([] (instant-kill-skills skills-list-page (:instant-kill skill-types)))
  ([url skill-types]
     (parse-skills url skill-types
                   (partial partition 6)
                   (fn [skill-type skill]
                     (let [[name rank mp fatal-chance target _] skill]
                       {:name name :rank rank :target target
                        :mp (Integer/parseInt mp)
                        :fatal-chance (percents->double fatal-chance)
                        :alignment-type (get-type skill-type)})))))

(defn ailment-skills
  ([] (ailment-skills skills-list-page (:ailment skill-types)))
  ([url skill-types]
     (parse-skills url skill-types
                   (partial partition 8)
                   (fn [skill-type skill]
                     (let [[name rank mp target chance ailment remark _] skill]
                       {:name name :rank rank :mp (Integer/parseInt mp)
                        :target target :ailment ailment :remark remark
                        :chance (percents->double chance)})))))

(defn support-skills
  ([] (support-skills skills-list-page (:support skill-types)))
  ([url skill-types]
     (parse-skills url skill-types
                   (partial partition 6)
                   (fn [skill-type skill]
                     (let [[name rank mp target effect _] skill]
                       {:name name :rank rank :mp (Integer/parseInt mp)
                        :target target :effect effect})))))

(defn- skills-with-effect
  [url skill-types]
  (parse-skills url skill-types
                (partial partition 5)
                (fn [skill-type skill]
                  (let [[name rank mp target effect] skill]
                    {:name name :rank rank :mp (Integer/parseInt mp)
                     :target target :effect effect}))))

(defn stat-modifier-skills
  ([] (stat-modifier-skills skills-list-page (:stat-modifier skill-types)))
  ([url skill-types] (skills-with-effect url skill-types)))

(defn healing-skills
  ([] (healing-skills skills-list-page (:healing skill-types)))
  ([url skill-types] (skills-with-effect url skill-types)))

(defn auto-skills
  ([] (auto-skills skills-list-page (:auto skill-types)))
  ([url skill-types]
     (parse-skills url skill-types
                   (partial partition 3)
                   (fn [skill-type skill]
                     (let [[name rank effect] skill]
                       {:name name :rank rank :effect effect})))))

(defn demons-list
  ([] (demons-list demons-list-page))
  ([url]
     (into {} (map (juxt html/text #(get-in % [:attrs :href]))
                   (demons-list-html url)))))

(defn- parse-demon-skills
  [demon-skills-html]
  (-> demon-skills-html
      (html/select [:tr])
      (#(drop 2 %))
      (html/select [:th])
      (#(map html->text %))))

(defn- parse-two-row-table
  [table-html]
  (-> table-html
      (html/select [:tr])
      rest
      (html/select [:td])
      (#(map html->text %))))

(defn- parse-demon-stats
  [demon-stats-html]
  (parse-two-row-table demon-stats-html))

(defn- parse-demon-resistances
  [demon-resistances-html]
  (parse-two-row-table demon-resistances-html))

(defn- parse-demon-ailment-and-attack
  [demon-ailment-and-attack-html]
  (-> demon-ailment-and-attack-html
      (html/select [:tr])
      (html/select [:td])
      (#(map html->text %))))

(defn demon
  [name url]
  (println name url)
  (let [[stats resistances ailment-and-attack skills :as d] (demon-html url)]
    (when-not (empty? d)
      (let [stats (parse-demon-stats stats)
            race (first stats)
            [level hp mp st dx ma ag lu]
            (map #(try (Integer/parseInt %)
                       (catch NumberFormatException e 0)) (rest stats))
            [phys gun fire ice elec force light dark]
            (parse-demon-resistances resistances)
            [ailment attack]
            (parse-demon-ailment-and-attack ailment-and-attack)]
        {:name name :race race :level level :hp hp :mp mp
         :stats {:strength st :dexterity dx :magic ma :agility ag :luck lu}
         :resistances {:physical phys :gun gun :fire fire :ice ice
                       :electric elec :force force :light light :dark dark}
         :ailment ailment :attack attack
         :skills (parse-demon-skills skills)}))))
