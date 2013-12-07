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
 (defclass Skill)
 (defclass Race)
 (defclass Demon))

(as-subclasses
 ElementalNature
 :disjoint :cover
 (declare-classes Physical Gun Fire Ice Electric Force Light Dark Almighty))

(as-subclasses
 Skill
 :disjoint :cover
 (declare-classes
  AttackSkill InstantKillSkill AilmentSkill SupportSkill
  StatModifierSkill HealingSKill AutoSkill))

(as-subclasses
 Race
 :disjoint :cover
 (declare-classes
  Deity Amatsu Megami Nymph Fury Kunitsu Kishin Zealot Lady Reaper Vile Tyrant
  Genma Yoma Fairy Night Herald Divine Fallen Avian Flight Raptor Jirae Brute
  Femme Jaki Dragon Snake Drake Avatar Holy Food Beast Wilder Tree Wood Vermin
  Ghost Foul Spirit Undead Element Fiend Famed Enigma Entity Godly Chaos Cyber
  Human Undead Hordes Mitama))

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
 (defoproperty hasWeakness
   :domain Demon
   :range ElementalNature)
 (defoproperty isWeaknessOf
   :domain ElementalNature
   :range Demon))

(as-inverse
 (defoproperty strongAgainst
   :domain Demon
   :range ElementalNature)
 (defoproperty isStrengthOf
   :domain ElementalNature
   :range Demon))

(as-inverse
 (defoproperty hasSkill
   :domain Demon
   :range Skill)
 (defoproperty belongsTo
   :domain Skill
   :range Demon))

(as-inverse
 (defoproperty ofElement
   :characteristic functional
   :domain AttackSkill
   :range ElementalNature)
 (defoproperty isNatureOf
   :characteristic inversefunctional
   :domain ElementalNature
   :range AttackSkill))

(as-subclasses
 AttackSkill
 :disjoint :cover
 (defclass FireAttackSkill
   :equivalent
   (owland AttackSkill
           (owlsome ofElement Fire)))
 (defclass IceAttackSkill
   :equivalent
   (owland AttackSkill
           (owlsome ofElement Ice)))
 (defclass ForceAttackSkill
   :equivalent
   (owland AttackSkill
           (owlsome ofElement Force)))
 (defclass ElectricAttackSkill
   :equivalent
   (owland AttackSkill
           (owlsome ofElement Electric)))
 (defclass GunAttackSkill
   :equivalent
   (owland AttackSkill
           (owlsome ofElement Gun)))
 (defclass PhysicalAttackSkill
   :equivalent
   (owland AttackSkill
           (owlsome ofElement Physical)))
 (defclass AlmightyAttackSkill
   :equivalent
   (owland AttackSkill
           (owlsome ofElement Almighty))))

(as-subclasses
 InstantKillSkill
 :disjoint :cover
 (declare-classes LightKillSkill DarkKillSkill))


(defn- enum [& values] (apply oneof (map literal values)))

;;; TODO add restrictions to classes - what properties should they contain
;;; TODO should be minmax
(defdproperty hasRank :range rdf:plainliteral)
(defdproperty usesMp :range xsd:integer)
(defdproperty fatalChance :range (minmax 0.0 1.0))
(defdproperty hasHits :range rdf:plainliteral)
(defdproperty hasDamage :range (enum "Weak" "Medium" "Heavy" "Severe"))
(defdproperty hasTarget :range (enum "Single" "Multi" "All" "Self" "Ally"
                                     "Party" "Foes" "Universal"))
(defdproperty hasRemark :range rdf:plainliteral)
;;; TODO should be list of enums
(defdproperty hasAilment :range rdf:plainliteral)
(defdproperty ailmentChance :range (minmax 0.0 1.0))
(defdproperty hasEffect :range xsd:integer)

(doseq [attack-skill-map (crawl/attack-skills)]
  (let [{:keys [name rank mp damage hits target remark element]}
        attack-skill-map]
    (eval
     `(defindividual ~(symbol (cstr/replace name " " "_"))
        :type ~(symbol (str element "AttackSkill"))
        :fact [(fact hasRank ~rank) (fact usesMp ~mp) (fact hasHits ~hits)
               (fact hasDamage ~damage) (fact hasTarget ~target)
               (fact hasRemark ~remark)]))))

(doseq [instant-kill-skill-map (crawl/instant-kill-skills)]
  (let [{:keys [name rank mp target fatal-chance alignment-type]}
        instant-kill-skill-map]
    (eval
     `(defindividual ~(symbol (cstr/replace name " " "_"))
        :type ~(symbol (str alignment-type "KillSkill"))
        :fact [(fact hasRank ~rank) (fact usesMp ~mp) (fact hasTarget ~target)
               (fact fatalChance ~fatal-chance)]))))

(doseq [ailment-skill-map (crawl/ailment-skills)]
  (let [{:keys [name rank mp target chance remark ailment]}
        ailment-skill-map]
    (eval
     `(defindividual ~(symbol (cstr/replace name " " "_"))
        :type AilmentSkill
        :fact [(fact hasRank ~rank) (fact usesMp ~mp) (fact hasTarget ~target)
               (fact ailmentChance ~chance) (fact hasRemark ~remark)
               (fact hasAilment ~ailment)]))))

(doseq [support-skill-map (crawl/support-skills)]
  (let [{:keys [name rank mp target effect]} support-skill-map]
    (eval
     `(defindividual ~(symbol (cstr/replace name " " "_"))
        :type SupportSkill
        :fact [(fact hasRank ~rank) (fact usesMp ~mp) (fact hasTarget ~target)
               (fact hasEffect ~effect)]))))

(doseq [stat-modifier-skills-map (crawl/stat-modifier-skills)]
  (let [{:keys [name rank mp target effect]} stat-modifier-skills-map]
    (eval
     `(defindividual ~(symbol (cstr/replace name " " "_"))
        :type StatModifierSkill
        :fact [(fact hasRank ~rank) (fact usesMp ~mp) (fact hasTarget ~target)
               (fact hasEffect ~effect)]))))
