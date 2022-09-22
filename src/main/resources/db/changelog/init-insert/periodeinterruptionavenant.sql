INSERT INTO `PeriodeInterruptionAvenant` (`idAvenant`, `dateDebutInterruption`, `dateFinInterruption`, `isModif`)
SELECT `Avenant`.`idAvenant`, `Avenant`.`dateDebutInterruption`, `Avenant`.`dateFinInterruption`, false
FROM `Avenant` LEFT JOIN `PeriodeInterruptionAvenant` ON `PeriodeInterruptionAvenant`.`idAvenant` = `Avenant`.`idAvenant`
WHERE `Avenant`.`interruptionStage` = 1 AND `Avenant`.`dateDebutInterruption` IS NOT NULL AND `Avenant`.`dateFinInterruption` IS NOT NULL AND `PeriodeInterruptionAvenant`.`idAvenant` IS NULL;
