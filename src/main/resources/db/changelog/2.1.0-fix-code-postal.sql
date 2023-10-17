update Commune set codePostal = concat('0', codePostal) where length(codePostal) < 5;
