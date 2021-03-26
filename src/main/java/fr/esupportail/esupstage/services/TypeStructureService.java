package fr.esupportail.esupstage.services;

import fr.esupportail.esupstage.domain.jpa.entities.Effectif;
import fr.esupportail.esupstage.domain.jpa.entities.TypeStructure;
import fr.esupportail.esupstage.domain.jpa.repositories.EffectifRepository;
import fr.esupportail.esupstage.domain.jpa.repositories.TypeStructureRepository;
import fr.esupportail.esupstage.exception.NotFoundException;
import fr.esupportail.esupstage.services.beans.EffectifBean;
import fr.esupportail.esupstage.services.beans.TypeStructureBean;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TypeStructureService {

	private final TypeStructureRepository typeStructureRepository;

	@Autowired
	public TypeStructureService(final TypeStructureRepository typeStructureRepository) {
		super();
		this.typeStructureRepository = typeStructureRepository;
	}

	public Page<TypeStructureBean> findAll(final Pageable pageable) {
		return this.typeStructureRepository.findAll(pageable).map(TypeStructureService::convert);
	}

	public TypeStructureBean findById(final Integer id) {
		return this.typeStructureRepository.findById(id).map(TypeStructureService::convert)
				.orElseThrow(NotFoundException::new);
	}

	public TypeStructureBean save(final TypeStructureBean bean) {
		return TypeStructureService.convert(this.typeStructureRepository.save(TypeStructureService.convert(bean)));
	}

	public void deleteById(final Integer id) {
		this.typeStructureRepository.deleteById(id);
	}

	public static TypeStructureBean convert(final TypeStructure feed) {
		final TypeStructureBean result = new TypeStructureBean();
		BeanUtils.copyProperties(feed, result);
		return result;
	}

	public static TypeStructure convert(final TypeStructureBean feed) {
		final TypeStructure result = new TypeStructure();
		BeanUtils.copyProperties(feed, result);
		return result;
	}

}
