INSERT INTO `PeriodeInterruptionStage` (`idConvention`, `dateDebutInterruption`, `dateFinInterruption`)
SELECT `idConvention`, `dateDebutInterruption`, `dateFinInterruption`
FROM `Convention`
WHERE `interruptionStage` = 1 AND `dateDebutInterruption` IS NOT NULL AND `dateFinInterruption` IS NOT NULL;
