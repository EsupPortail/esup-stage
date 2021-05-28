package fr.esupportail.esupstage.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import fr.esupportail.esupstage.domain.jpa.repositories.CentreGestionRepository;
import fr.esupportail.esupstage.services.beans.CentreGestionBean;
import fr.esupportail.esupstage.services.beans.CentreGestionMapper;

@Service
public class CentreGestionService {

	private final CentreGestionRepository centreGestionRepository;

	@Autowired
	public CentreGestionService(final CentreGestionRepository centreGestionRepository) {
		super();
		this.centreGestionRepository = centreGestionRepository;
	}

	public Page<CentreGestionBean> findAll(Pageable pageable) {
		return this.centreGestionRepository.findAll(pageable).map(CentreGestionMapper.INSTANCE::convert);
	}

}
