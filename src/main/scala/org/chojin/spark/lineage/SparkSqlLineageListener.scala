package org.chojin.spark.lineage

import grizzled.slf4j.Logger
import org.apache.spark.sql.execution.QueryExecution
import org.apache.spark.sql.util.QueryExecutionListener
import org.chojin.spark.lineage.reporter.Reporter

case class SparkSqlLineageListener(reporters: List[Reporter], async: Boolean = true) extends QueryExecutionListener {
  private lazy val LOGGER = Logger[this.type]
  private lazy val processor = new ReportProcessor(reporters)

  def this() = this(Config.createInstancesOf("reporter"))

  if (async) {
    processor.runInBackground()
  }

  override def onSuccess(funcName: String, qe: QueryExecution, durationNs: Long): Unit = {
    LOGGER.debug(s"Logical plan:\n${qe.logical}")

    LOGGER.info("Offering query execution to report processor")
    processor.offer(qe, async)
  }

  override def onFailure(funcName: String, qe: QueryExecution, exception: Exception): Unit = {
  }
}
