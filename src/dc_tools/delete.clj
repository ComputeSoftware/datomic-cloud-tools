(ns dc-tools.delete
  (:require
    [clojure.string :as str]
    [clojure.java.shell :as sh]
    [clojure.data.json :as json]))

(defn json-cmd
  ([args] (json-cmd args nil))
  ([args opts]
   (let [r (apply sh/sh (concat args (mapcat identity opts)))]
     (if (= 0 (:exit r))
       (json/read-str (:out r))
       (println "Error:" (:err r))))))

(defn parse-arn
  [arn-str]
  (let [parts (drop 1 (str/split arn-str #":" 6))]
    (zipmap [:partition :service :region :account-id :resource] parts)))

(defmulti delete! :service)

;; Delete docs
;; https://docs.datomic.com/cloud/operation/deleting.html#deleting-storage

(defn delete-s3-bucket
  [s3-bucket]
  (sh/sh "aws" "s3" "rb" (str "s3://" s3-bucket) "--force"))

(defmethod delete! "s3"
  [{:keys [resource]}]
  (println "Deleting S3 bucket" resource)
  (delete-s3-bucket resource))


(defn delete-ddb-table
  [table-name]
  (sh/sh "aws" "dynamodb" "delete-table" "--table-name" table-name))

(defmethod delete! "dynamodb"
  [{:keys [resource]}]
  (let [[_ table-name] (str/split resource #"\/" 2)]
    (println "Deleting ddb table" table-name)
    (delete-ddb-table table-name)))


(defn delete-efs
  [fs-id]
  (sh/sh "aws" "efs" "delete-file-system" "--file-system-id" fs-id))

(defmethod delete! "elasticfilesystem"
  [{:keys [resource]}]
  (let [[_ fs-id] (str/split resource #"\/" 2)]
    (println "Deleting efs " fs-id)
    (delete-efs fs-id)))


(defn delete-log-group
  [log-group-name]
  (sh/sh "aws" "logs" "delete-log-group" "--log-group-name" log-group-name))


(defn deregister-dynamo-scalable-target
  [datomic-system]
  (let [deregister (fn [dimension]
                     (sh/sh "aws" "application-autoscaling" "deregister-scalable-target"
                            "--service-namespace" "dynamodb"
                            "--scalable-dimension" dimension
                            "--resource-id" (format "table/datomic-%s" datomic-system)))]
    (deregister "dynamodb:table:WriteCapacityUnits")
    (deregister "dynamodb:table:ReadCapacityUnits")))

(defn get-datomic-resource-arns
  [system-name]
  (let [r (json-cmd ["aws" "resourcegroupstaggingapi" "get-resources" "--tag-filters"
                     (format "Key=datomic:system,Values=%s" system-name)])]
    (when r
      (map (comp parse-arn #(get % "ResourceARN")) (get r "ResourceTagMappingList")))))

(defn do-delete!
  [system-name parsed-arns]
  (doseq [arn-map parsed-arns]
    (delete! arn-map))

  (println "Deregistering dynamodb application-autoscaling")
  (deregister-dynamo-scalable-target system-name)

  (println "Deleting log group")
  (delete-log-group (str "datomic-" system-name)))

(defn delete-leftover-datomic-system-resources
  [system-name]
  (->> (get-datomic-resource-arns system-name)
       (do-delete! system-name)))

(defn delete-command
  [args]
  (if-let [system-name (first args)]
    (do
      (delete-leftover-datomic-system-resources system-name)
      {:success? true})
    {:success? false
     :msg      "Missing datomic system name."}))