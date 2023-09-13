INSERT INTO CentreGestionSignataire (idCentreGestion, signataire, ordre, type)
SELECT idCentreGestion, REPLACE(JSON_EXTRACT(ordreSignature, '$[0]'), '"', ''), 1, 'otp'
FROM CentreGestion;

INSERT INTO CentreGestionSignataire (idCentreGestion, signataire, ordre, type)
SELECT idCentreGestion, REPLACE(JSON_EXTRACT(ordreSignature, '$[1]'), '"', ''), 2, 'otp'
FROM CentreGestion;

INSERT INTO CentreGestionSignataire (idCentreGestion, signataire, ordre, type)
SELECT idCentreGestion, REPLACE(JSON_EXTRACT(ordreSignature, '$[2]'), '"', ''), 3, 'otp'
FROM CentreGestion;

INSERT INTO CentreGestionSignataire (idCentreGestion, signataire, ordre, type)
SELECT idCentreGestion, REPLACE(JSON_EXTRACT(ordreSignature, '$[3]'), '"', ''), 4, 'otp'
FROM CentreGestion;

INSERT INTO CentreGestionSignataire (idCentreGestion, signataire, ordre, type)
SELECT idCentreGestion, REPLACE(JSON_EXTRACT(ordreSignature, '$[4]'), '"', ''), 5, 'otp'
FROM CentreGestion;

ALTER TABLE CentreGestion DROP COLUMN ordreSignature;
