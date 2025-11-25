INSERT INTO ConventionNomenclature (idConvention, langueConvention, devise, modeValidationStage, modeVersGratification, natureTravail, origineStage, tempsTravail, theme, typeConvention, uniteDureeExceptionnelle, uniteDureeGratification, uniteGratification)
SELECT c.idConvention, lc.libelleLangueConvention, d.libelleDevise, mvs.libelleModeValidationStage, mvg.libelleModeVersGratification, nt.libelleNatureTravail, os.libelleOrigineStage, tt.libelleTempsTravail, t.libelleTheme, tc.libelleTypeConvention, ude.libelleUniteDuree, udg.libelleUniteDuree, ug.libelleUniteGratification
FROM Convention c
    LEFT JOIN LangueConvention lc ON lc.codeLangueConvention = c.codeLangueConvention
    LEFT JOIN Devise d ON d.idDevise = c.idDevise
    LEFT JOIN ModeValidationStage mvs ON mvs.idModeValidationStage = c.idModeValidationStage
    LEFT JOIN ModeVersGratification mvg ON mvg.idModeVersGratification = c.idModeVersGratification
    LEFT JOIN NatureTravail nt ON nt.idNatureTravail = c.idNatureTravail
    LEFT JOIN OrigineStage os ON os.idOrigineStage = c.idOrigineStage
    LEFT JOIN TempsTravail tt ON tt.idTempsTravail = c.idTempsTravail
    LEFT JOIN Theme t ON t.idTheme = c.idTheme
    LEFT JOIN TypeConvention tc ON tc.idTypeConvention = c.idTypeConvention
    LEFT JOIN UniteDuree ude ON ude.idUniteDuree = c.idUniteDureeExceptionnelle
    LEFT JOIN UniteDuree udg ON udg.idUniteDuree = c.idUniteDureeGratification
    LEFT JOIN UniteGratification ug ON ug.idUniteGratification = c.idUniteGratification