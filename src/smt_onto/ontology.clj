(ns smt-onto.ontology
  (:require [tawny [owl :refer :all]]))

(defontology smt-ontology
  :iri "https://github.com/gsnewmark/smt-onto/resources/smt-onto.owl"
  :prefix "smt:"
  :comment "An ontology describing SMT IV demons."
  :versioninfo "Unreleased Version")

(as-disjoint
 (defclass Element)
 (defclass Skill)
 (defclass Race)
 (defclass Demon))

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
   :range Element)
 (defoproperty isWeaknessOf
   :domain Element
   :range Demon))

(as-inverse
 (defoproperty strongAgainst
   :domain Demon
   :range Element)
 (defoproperty isStrengthOf
   :domain Element
   :range Demon))

(as-inverse
 (defoproperty hasSkill
   :domain Demon
   :range Skill)
 (defoproperty belongsTo
   :domain Skill
   :range Demon))

;; TODO domain should be (not yet defined) AttackSkill
(as-inverse
 (defoproperty ofElement
   :characteristic functional
   :domain Skill
   :range Element)
 (defoproperty isNatureOf
   :characteristic inversefunctional
   :domain Element
   :range Skill))
