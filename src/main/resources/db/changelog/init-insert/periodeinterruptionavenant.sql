INSERT INTO `PeriodeInterruptionAvenant` (`idAvenant`, `dateDebutInterruption`, `dateFinInterruption`, `isModif`)
SELECT `idAvenant`, `dateDebutInterruption`, `dateFinInterruption`, false
FROM `Avenant`
WHERE `interruptionStage` = 1 AND `dateDebutInterruption` IS NOT NULL AND `dateFinInterruption` IS NOT NULL;
