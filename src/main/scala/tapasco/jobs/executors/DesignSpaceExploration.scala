//
// Copyright (C) 2017 Jens Korinth, TU Darmstadt
//
// This file is part of Tapasco (TPC).
//
// Tapasco is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Tapasco is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with Tapasco.  If not, see <http://www.gnu.org/licenses/>.
//
/**
 * @file     DesignSpaceExploration.scala
 * @brief    DesignSpaceExploration executor.
 * @authors  J. Korinth, TU Darmstadt (jk@esa.cs.tu-darmstadt.de)
 **/
package de.tu_darmstadt.cs.esa.tapasco.jobs.executors
import  de.tu_darmstadt.cs.esa.tapasco.base._
import  de.tu_darmstadt.cs.esa.tapasco.task._
import  de.tu_darmstadt.cs.esa.tapasco.jobs._
import  de.tu_darmstadt.cs.esa.tapasco.jobs.json._
import  play.api.libs.json._
import  java.util.concurrent.Semaphore

private object DesignSpaceExploration extends Executor[DesignSpaceExplorationJob] {
  private implicit val logger = de.tu_darmstadt.cs.esa.tapasco.Logging.logger(getClass)

  def execute(job: DesignSpaceExplorationJob)
             (implicit cfg: Configuration, tsk: Tasks): Boolean = {
    logger.debug("job: {}", Json.prettyPrint(Json.toJson(job)))
    val signal = new Semaphore(0)

    // first, collect all kernels and trigger HLS if not built yet
    val kernels = job.initialComposition.composition map (_.kernel) toSet

    logger.debug("kernels found in composition: {}", kernels)

    // run HLS job first to build all kernels (will skip existing ones)
    val hls_ok = HighLevelSynthesisJob(
      "VivadoHLS", // FIXME
      if (job.architectures.size > 0) Some(job.architectures.toSeq map (_.name) sorted) else None,
      if (job.platforms.size > 0) Some(job.platforms.toSeq map (_.name) sorted) else None,
      Some(kernels.toSeq.sorted)
    ).execute

    if (hls_ok) {
      val tasks = for {
        a <- job.architectures.toSeq.sortBy(_.name)
        p <- job.platforms.toSeq.sortBy(_.name)
        target = Target(a, p)
      } yield mkExplorationTask(job, target, _ => signal.release())

      tasks foreach { tsk.apply _ }

      0 until tasks.length foreach { i =>
        signal.acquire()
        logger.debug("DSE task #{} collected", i)
      }

      logger.info("all DSE tasks have finished")

      (tasks map (_.result) fold true) (_ && _)
    } else {
      logger.error("HLS tasks failed, aborting composition")
      false
    }
  }

  private def mkExplorationTask(job: DesignSpaceExplorationJob, t: Target, onComplete: Boolean => Unit)
                               (implicit cfg: Configuration, tsk: Tasks): ExplorationTask =
    DesignSpaceExplorationTask(
      job.initialComposition,
      t,
      job.dimensions,
      job.initialFrequency,
      job.heuristic,
      job.batchSize,
      job.basePath map (_.toString),
      job.features,
      None, // logfile
      job.debugMode,
      onComplete
    )
}