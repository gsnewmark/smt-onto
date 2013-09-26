(ns smt-onto.ontology
  (:require [tawny [owl :refer :all]]))

(defontology smt-ontology
  :iri "https://github.com/gsnewmark/smt-onto/resources/smt-onto.owl"
  :prefix "smt:"
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
 (declare-classes Physical Gun Fire Ice Electric Force Light Dark))

(as-subclasses
 Skill
 :disjoint :cover
 (declare-classes Attack Almighty Ailment Support StatModifier Healing Auto))

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
   :domain Attack
   :range ElementalNature)
 (defoproperty isNatureOf
   :characteristic inversefunctional
   :domain ElementalNature
   :range Attack))
