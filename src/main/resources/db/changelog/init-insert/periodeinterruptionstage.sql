INSERT INTO `PeriodeInterruptionStage` (`idConvention`, `dateDebutInterruption`, `dateFinInterruption`)
SELECT `idConvention`, `dateDebutInterruption`, `dateFinInterruption`
FROM `Convention`
WHERE `interruptionStage` = 1;
