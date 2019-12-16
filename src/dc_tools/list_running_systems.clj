(ns dc-tools.list-running-systems
  (:require
    [clojure.java.shell :as sh]))

(defn list-sh
  []
  (let [{:keys [exit out err]}
        (sh/sh "aws" "ec2" "describe-instances"
               #_#_"--region" ""
               "--filters" "Name=tag-key,Values=datomic:tx-group"
               "Name=instance-state-name,Values=running"
               "--query" "Reservations[*].Instances[*].[Tags[?Key==`datomic:system`].Value]"
               "--output" "text")]
    (if (= exit 0)
      {:success? true
       :msg out}
      {:success? false
       :msg      (str "[Error] Failed to list running systems. \n" err)})))

(defn run-command
  [args]
  (list-sh))