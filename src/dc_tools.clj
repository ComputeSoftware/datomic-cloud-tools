(ns dc-tools
  (:require
    [clojure.string :as str]
    [dc-tools.delete-storage :as delete]
    [dc-tools.list-running-systems :as list-running-systems]))

(defn system-exit!
  "Exit the process."
  [status message]
  (when (and message (not (str/blank? message)))
    (binding [*out* (if (= 0 status) *out* *err*)]
      (println message)))
  (System/exit status))

(defn run-command!
  [args]
  (let [command (first args)
        cmd-args (rest args)]
    (case command
      "delete-storage" (delete/delete-command cmd-args)
      "list-running-systems" (list-running-systems/run-command cmd-args)
      {:success? false
       :msg      (format "Unknown command %s" command ".")})))

(defn -main
  [& args]
  (let [result (run-command! args)]
    (shutdown-agents)
    (system-exit! (if (:success? result) 0 1) (:msg result))))