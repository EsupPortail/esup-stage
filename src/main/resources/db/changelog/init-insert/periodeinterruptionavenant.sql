INSERT INTO `PeriodeInterruptionAvenant` (`idAvenant`, `dateDebutInterruption`, `dateFinInterruption`, `isModif`)
SELECT `idAvenant`, `dateDebutInterruption`, `dateFinInterruption`, false
FROM `Avenant`
WHERE `interruptionStage` = 1;
