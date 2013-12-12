(ns smt-onto.ontology
  (:require [clojure.string :as cstr]
            [smt-onto.crawl :as crawl]
            [tawny [owl :refer :all]
                   [pattern :as p]]))

(defontology smt-ontology
  :iri "https://github.com/gsnewmark/smt-onto/resources/smt-onto.owl"
  :prefix "smtIV:"
  :comment "An ontology describing SMT IV demons."
  :versioninfo "Unreleased Version")

(as-disjoint
 (defclass ElementalNature)
 (defclass Skill
   :equivalent
   (owland (owlsome belongsTo Demon)
           (exactly 1 hasRank)))
 (defclass Race)
 (defclass Demon
   :equivalent
   (owland (owlsome ofRace Race)
           (atleast 1 hasSkill Skill)
           (owlsome initialLevel xsd:integer)
           (owlsome hasHP xsd:integer)
           (owlsome hasMP xsd:integer)
           (owlsome hasStrength xsd:integer)
           (owlsome hasDexterity xsd:integer)
           (owlsome hasMagic xsd:integer)
           (owlsome hasAgility xsd:integer)
           (owlsome hasLuck xsd:integer)
           (owlsome hasAilmentResistance rdf:plainliteral)
           (owlsome hasAttack rdf:plainliteral))))

(as-subclasses
 Skill
 :disjoint :cover
 (defclass ElementalSkill
   :equivalent
   (owlsome ofElement ElementalNature))
 (defclass AilmentSkill
   :equivalent
   (owland (owlsome usesMp xsd:integer)
           (owlsome hasRemark rdf:plainliteral)
           (owlsome hasTarget rdf:plainliteral)
           (owlsome ailmentChance xsd:double)
           (owlsome hasAilment rdf:plainliteral)))
 (defclass SupportSkill
   :subclass
   (owland (owlsome usesMp xsd:integer)
           (owlsome hasTarget rdf:plainliteral)
           (owlsome hasEffect rdf:plainliteral)))
 (defclass StatModifierSkill
   :subclass
   (owland (owlsome usesMp xsd:integer)
           (owlsome hasTarget rdf:plainliteral)
           (owlsome hasEffect rdf:plainliteral)))
 (defclass HealingSkill
   :subclass
   (owland (owlsome usesMp xsd:integer)
           (owlsome hasTarget rdf:plainliteral)
           (owlsome hasEffect rdf:plainliteral)))
 (defclass AutoSkill))

(as-subclasses
 ElementalSkill
 :disjoint :cover
 (defclass AttackSkill
   :equivalent
   (owland (owlsome usesMp xsd:integer)
           (owlsome hasRemark rdf:plainliteral)
           (owlsome hasTarget rdf:plainliteral)
           (owlsome hasHits rdf:plainliteral)
           (owlsome hasDamage rdf:plainliteral)))
 (defclass InstantKillSkill
   :equivalent
   (owland (owlsome usesMp xsd:integer)
           (owlsome hasTarget rdf:plainliteral)
           (owlsome fatalChance xsd:double))))

(doseq [element ["Physical" "Gun" "Fire" "Ice" "Electric" "Force"
                 "Light" "Dark" "Almighty"]]
  (eval `(defindividual ~(symbol element) :type ElementalNature)))

(doseq [race ["Deity" "Amatsu" "Megami" "Nymph" "Fury" "Kunitsu" "Kishin"
              "Zealot" "Lady" "Reaper" "Vile" "Tyrant" "Genma" "Yoma"
              "Fairy" "Night" "Herald" "Divine" "Fallen" "Avian" "Flight"
              "Raptor" "Jirae" "Brute" "Femme" "Jaki" "Dragon" "Snake"
              "Drake" "Avatar" "Holy" "Food" "Beast" "Wilder" "Tree" "Wood"
              "Vermin" "Ghost" "Foul" "Spirit" "Undead" "Element" "Fiend"
              "Famed" "Enigma" "Entity" "Godly" "Chaos" "Cyber" "Human"
              "Undead" "Hordes" "Mitama"]]
  (eval `(defindividual ~(symbol race) :type Race)))

(as-inverse
 (defoproperty ofRace
   :characteristic functional
   :domain Demon
   :range Race)
 (defoproperty hasMember
   :characteristic inversefunctional
   :domain Race
   :range Demon))

(as-inverse
 (defoproperty hasSkill
   :domain Demon
   :range Skill)
 (defoproperty belongsTo
   :domain Skill
   :range Demon))

(as-inverse
 (defoproperty weakTo
   :domain Demon
   :range ElementalNature)
 (defoproperty isWeaknessOf
   :domain ElementalNature
   :range Demon))
(as-inverse
 (defoproperty resists
   :domain Demon
   :range ElementalNature)
 (defoproperty isResistedBy
   :domain ElementalNature
   :range Demon))
(as-inverse
 (defoproperty drains
   :domain Demon
   :range ElementalNature)
 (defoproperty isDrainedBy
   :domain ElementalNature
   :range Demon))
(as-inverse
 (defoproperty nullifies
   :domain Demon
   :range ElementalNature)
 (defoproperty isNullifiedBy
   :domain ElementalNature
   :range Demon))
(as-inverse
 (defoproperty repels
   :domain Demon
   :range ElementalNature)
 (defoproperty isRepelledBy
   :domain ElementalNature
   :range Demon))

(as-inverse
 (defoproperty ofElement
   :characteristic functional
   :domain ElementalSkill
   :range ElementalNature)
 (defoproperty isNatureOf
   :characteristic inversefunctional
   :domain ElementalNature
   :range ElementalSkill))

(as-subclasses
 AttackSkill
 :disjoint :cover
 (defclass FireAttackSkill
   :equivalent
   (owland AttackSkill
           (hasvalue ofElement Fire)))
 (defclass IceAttackSkill
   :equivalent
   (owland AttackSkill
           (hasvalue ofElement Ice)))
 (defclass ForceAttackSkill
   :equivalent
   (owland AttackSkill
           (hasvalue ofElement Force)))
 (defclass ElectricAttackSkill
   :equivalent
   (owland AttackSkill
           (hasvalue ofElement Electric)))
 (defclass GunAttackSkill
   :equivalent
   (owland AttackSkill
           (hasvalue ofElement Gun)))
 (defclass PhysicalAttackSkill
   :equivalent
   (owland AttackSkill
           (hasvalue ofElement Physical)))
 (defclass AlmightyAttackSkill
   :equivalent
   (owland AttackSkill
           (hasvalue ofElement Almighty))))

(as-subclasses
 InstantKillSkill
 :disjoint :cover
 (defclass LightKillSkill
   :equivalent
   (owland InstantKillSkill
           (hasvalue ofElement Light)))
 (defclass DarkKillSkill
   :equivalent
   (owland InstantKillSkill
           (hasvalue ofElement Dark))))

(defn- enum [& values] (apply oneof (map literal values)))

;;; TODO should be minmax
(defdproperty hasRank :range rdf:plainliteral)
(defdproperty usesMp :range xsd:integer)
(defdproperty fatalChance :range (minmax 0.0 1.0))
(defdproperty hasHits :range rdf:plainliteral)
(defdproperty hasDamage :range (enum "Weak" "Medium" "Heavy" "Severe"
                                     "1" "666"))
(defdproperty hasTarget :range (enum "Single" "Multi" "All" "Self" "Ally"
                                     "Party" "Foes" "Universal"))
(defdproperty hasRemark :range rdf:plainliteral)
;;; TODO should be list of enums
(defdproperty hasAilment :range rdf:plainliteral)
(defdproperty ailmentChance :range (minmax 0.0 1.0))
(defdproperty hasEffect :range rdf:plainliteral)
(defdproperty initialLevel :range (minmax 1 100))
(defdproperty hasHP :range xsd:integer)
(defdproperty hasMP :range xsd:integer)
(defdproperty hasStrength :range xsd:integer)
(defdproperty hasDexterity :range xsd:integer)
(defdproperty hasMagic :range xsd:integer)
(defdproperty hasAgility :range xsd:integer)
(defdproperty hasLuck :range xsd:integer)
(defdproperty hasAilmentResistance :range rdf:plainliteral)
(defdproperty hasAttack :range rdf:plainliteral)


(defn- space->_ [s] (cstr/replace s " " "_"))

(defn- capitalize-first
  [s]
  (let [[f r] (split-at 1 s)]
    (str (cstr/capitalize (apply str f)) (apply str r))))

(defn- capitalize-first-each-word
  [s]
  (cstr/join " " (map capitalize-first (cstr/split s #" "))))

(def resist-type->prop
  {"Weak" "weakTo" "Wk" "weakTo" "Null" "nullifies" "Nu" "nullifies"
   "Drain" "drains" "Dr" "drains" "Repel" "repels" "Reflect" "repels"
   "Resist" "resists" "Resistant" "resists" "Rs" "resists"})

;;; TODO uncrawled skills (most are boss/enemy specific)
(defn- filter-skills
  [skills]
  (remove #{"Crushing Blow" "Carol Hit" "Barrage" "Ancient Curse"
            "Strange Ray" "Horrible Ray" "Crushing Wave" "Macca Beam"
            "Wastrel Beam" "Dorn Gift" "Labrys Strike" "Snake's Fangs"
            "Queen's Feast" "Orchard Guardian" "Hell's Torment"
            "Ameno Murakumo" "Homeland Song" "Sunny Ray" "Vulnera"
            "Conquerer Spirit" "Deceit Chain" "Naught Wave" "Blank Bullet"
            "Impossible Slash" "Light Wing" "Chariot" "Hexagram"
            "Shalt Not Resist" "Evil Shine" "Kingly One" "Morning Star" ""
            ;; These could be present in allies
            "Spirit Focus" "Null Disease"}
          skills))

(defn- sanitize-skill
  [skill]
  (or ({"Iron Judgement" "Iron Judgment"
        "Diaharan" "Diarahan"} skill)
      skill))

(defn- fact-for-skill
  [skill]
  (let [name (space->_ (capitalize-first-each-word skill))]
    `(fact hasSkill ~(symbol name))))

(defn- fact-for-resistance
  [[element itype]]
  (let [element ((comp symbol cstr/capitalize name) element)
        type (resist-type->prop itype)]
    (when type `(fact ~(symbol type) ~element))))

(doseq [attack-skill-map (crawl/attack-skills)
        :let [{:keys [name rank mp damage hits target remark element]}
              attack-skill-map]]
  (eval
   `(defindividual ~(symbol (space->_ (capitalize-first-each-word name)))
      :type ~(symbol (str element "AttackSkill"))
      :fact [(fact hasRank ~rank) (fact usesMp ~mp) (fact hasHits ~hits)
             (fact hasDamage ~damage) (fact hasTarget ~target)
             (fact hasRemark ~remark)])))

(doseq [instant-kill-skill-map (crawl/instant-kill-skills)
        :let [{:keys [name rank mp target fatal-chance alignment-type]}
              instant-kill-skill-map]]
  (eval
   `(defindividual ~(symbol (space->_ (capitalize-first-each-word name)))
      :type ~(symbol (str alignment-type "KillSkill"))
      :fact [(fact hasRank ~rank) (fact usesMp ~mp) (fact hasTarget ~target)
             (fact fatalChance ~fatal-chance)])))

(doseq [ailment-skill-map (crawl/ailment-skills)
        :let [{:keys [name rank mp target chance remark ailment]}
              ailment-skill-map]]
  (eval
   `(defindividual ~(symbol (space->_ (capitalize-first-each-word name)))
      :type AilmentSkill
      :fact [(fact hasRank ~rank) (fact usesMp ~mp) (fact hasTarget ~target)
             (fact ailmentChance ~chance) (fact hasRemark ~remark)
             (fact hasAilment ~ailment)])))

(doseq [support-skill-map (crawl/support-skills)
        :let [{:keys [name rank mp target effect]} support-skill-map]]
  (eval
   `(defindividual ~(symbol (space->_ (capitalize-first-each-word name)))
      :type SupportSkill
      :fact [(fact hasRank ~rank) (fact usesMp ~mp) (fact hasTarget ~target)
             (fact hasEffect ~effect)])))

(doseq [stat-modifier-skills-map (crawl/stat-modifier-skills)
        :let [{:keys [name rank mp target effect]} stat-modifier-skills-map]]
  (eval
   `(defindividual ~(symbol (space->_ (capitalize-first-each-word name)))
      :type StatModifierSkill
      :fact [(fact hasRank ~rank) (fact usesMp ~mp) (fact hasTarget ~target)
             (fact hasEffect ~effect)])))

(doseq [healing-skills-map (crawl/healing-skills)
        :let [{:keys [name rank mp target effect]} healing-skills-map]]
  (eval
   `(defindividual ~(symbol (space->_ (capitalize-first-each-word name)))
      :type HealingSkill
      :fact [(fact hasRank ~rank) (fact usesMp ~mp) (fact hasTarget ~target)
             (fact hasEffect ~effect)])))

(doseq [auto-skills-map (crawl/auto-skills)
        :let [{:keys [name rank effect]} auto-skills-map]]
  (eval
   `(defindividual ~(symbol (space->_ (capitalize-first-each-word name)))
      :type AutoSkill
      :fact [(fact hasRank ~rank) (fact hasEffect ~effect)])))

(doseq [[name url] (crawl/demons-list)
        :let [{:keys [name race level hp mp stats
                      ailment attack resistances skills]
               :as demon}
              (crawl/demon name (str crawl/wiki-root url))

              {:keys [strength dexterity magic agility luck]} stats]]
  (when demon
    (eval
     ;; TODO find a way to overcome the following problem:
     ;;      names of some demons clash with Java classes (e. g., Long)
     `(defindividual ~(symbol (str (space->_ name) "_Demon"))
        :type Demon
        :fact [(fact ofRace ~(symbol race)) (fact initialLevel ~level)
               (fact hasHP ~hp) (fact hasMP ~mp)
               (fact hasStrength ~strength) (fact hasDexterity ~dexterity)
               (fact hasMagic ~magic) (fact hasAgility ~agility)
               (fact hasLuck ~luck)
               (fact hasAilmentResistance ~ailment) (fact hasAttack ~attack)
               ~@(remove nil? (map fact-for-resistance resistances))
               ~@(map (comp fact-for-skill sanitize-skill)
                      (filter-skills skills))]))))
