package fr.esupportail.esupstage.services;

import java.util.List;

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

	public List<CentreGestionBean> findAll() {
		return findAll(Pageable.unpaged()).toList();
	}

	public CentreGestionBean save(final CentreGestionBean toSave) {
		final CentreGestionMapper mapper = CentreGestionMapper.INSTANCE;
		return mapper.convert(this.centreGestionRepository.save(mapper.convert(toSave)));
	}

}
