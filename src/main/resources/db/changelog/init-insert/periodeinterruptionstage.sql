INSERT INTO `PeriodeInterruptionStage` (`idConvention`, `dateDebutInterruption`, `dateFinInterruption`)
SELECT `Convention`.`idConvention`, `Convention`.`dateDebutInterruption`, `Convention`.`dateFinInterruption`
FROM `Convention` LEFT JOIN `PeriodeInterruptionStage` ON `PeriodeInterruptionStage`.`idConvention` = `Convention`.`idConvention`
WHERE `Convention`.`interruptionStage` = 1 AND `Convention`.`dateDebutInterruption` IS NOT NULL AND `Convention`.`dateFinInterruption` IS NOT NULL AND `PeriodeInterruptionStage`.`idConvention` IS NULL;
