package fr.esupportail.esupstage.services;

import fr.esupportail.esupstage.domain.jpa.entities.Effectif;
import fr.esupportail.esupstage.domain.jpa.entities.Structure;
import fr.esupportail.esupstage.domain.jpa.repositories.EffectifRepository;
import fr.esupportail.esupstage.domain.jpa.repositories.StructureRepository;
import fr.esupportail.esupstage.exception.NotFoundException;
import fr.esupportail.esupstage.services.beans.EffectifBean;
import fr.esupportail.esupstage.services.beans.StructureBean;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class EffectifService {
		private final EffectifRepository effectifRepository;

		@Autowired
		public EffectifService(final EffectifRepository effectifRepository) {
				super();
				this.effectifRepository = effectifRepository;
		}

		public Page<EffectifBean> findAll(final Pageable pageable) {
				return this.effectifRepository.findAll(pageable).map(EffectifService::convert);
		}

		public EffectifBean findById(final Integer id) {
				return this.effectifRepository.findById(id).map(EffectifService::convert)
						.orElseThrow(NotFoundException::new);
		}

		public EffectifBean save(final EffectifBean bean) {
				return EffectifService.convert(this.effectifRepository.save(EffectifService.convert(bean)));
		}

		public void deleteById(final Integer id) {
				this.effectifRepository.deleteById(id);
		}

		public static EffectifBean convert(final Effectif feed) {
				final EffectifBean result = new EffectifBean();
				BeanUtils.copyProperties(feed, result);
				return result;
		}

		public static Effectif convert(final EffectifBean feed) {
				final Effectif result = new Effectif();
				BeanUtils.copyProperties(feed, result);
				return result;
		}
}
