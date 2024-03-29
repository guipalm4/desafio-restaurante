package com.dbserver.desafioRastaurante.service;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dbserver.desafioRastaurante.dto.ProfissionalDTO;
import com.dbserver.desafioRastaurante.dto.RestauranteDTO;
import com.dbserver.desafioRastaurante.dto.ResultadoVotoDTO;
import com.dbserver.desafioRastaurante.dto.VotacaoDTO;
import com.dbserver.desafioRastaurante.dto.VotoDTO;
import com.dbserver.desafioRastaurante.entities.Profissional;
import com.dbserver.desafioRastaurante.entities.Restaurante;
import com.dbserver.desafioRastaurante.entities.Votacao;
import com.dbserver.desafioRastaurante.entities.Voto;
import com.dbserver.desafioRastaurante.repository.ProfissionalRepository;
import com.dbserver.desafioRastaurante.repository.RestauranteRepository;
import com.dbserver.desafioRastaurante.repository.VotacaoRepository;
import com.dbserver.desafioRastaurante.repository.VotoRepository;
import com.dbserver.desafioRastaurante.service.exceptions.ActionDeniedException;
import com.dbserver.desafioRastaurante.service.exceptions.ObjectNotFoundException;
import com.dbserver.desafioRastaurante.util.CalendarUtil;
import com.dbserver.desafioRastaurante.util.DateUtil;
import com.dbserver.desafioRastaurante.util.Validator;

@Service
public class VotacaoServiceImpl implements VotacaoService {

	@Autowired
	private VotacaoRepository votacaoRepository;

	@Autowired
	private ProfissionalRepository profissionalRepository;

	@Autowired
	private VotoRepository votoRepository;

	@Autowired
	private RestauranteRepository restauranteRepository;

	private static final Date HOJE = CalendarUtil.truncate(Calendar.getInstance()).getTime();

	private static final Integer SEMANA_ATUAL = Calendar.WEEK_OF_YEAR;

	@Override
	public Votacao iniciarVotacao(ProfissionalDTO profissionalDTO) {

		Votacao votacao = votacaoRepository.findByDataVotacao(HOJE);

		if (votacao==null) {
			Profissional profissionalEncontrado = localizarProfissional(profissionalDTO);
			return inserir(setParametrosIniciarVotacao(profissionalEncontrado));
		}
		throw new ActionDeniedException("Vota��o j� foi iniciada hoje");
	}

	private Votacao setParametrosIniciarVotacao(Profissional facilitador) {
		Votacao votacao = new Votacao();
		votacao.setDataVotacao(HOJE);
		votacao.setFacilitador(facilitador);
		votacao.setAtiva(Boolean.TRUE);
		return votacao;
	}

	public Votacao inserir(Votacao votacao) {
		votacao.setId(null);
		votacao = votacaoRepository.save(votacao);
		return votacao;
	}

	@Override
	public Voto votar(Integer idVotacao, VotoDTO votoDTO) {

		Votacao votacaoEncontrada = localizarVotacao(idVotacao);
		Profissional profissionalEncontrado = localizarProfissional(votoDTO.getProfissionalDTO());

		if (!jaVotou(profissionalEncontrado)) {
			Restaurante restauranteEncontrado = localizarRestaurante(votoDTO.getRestauranteDTO());
			if (validarVotoRestaurante(restauranteEncontrado)) {
				Voto novoVoto = votoRepository
						.save(setParametorNovoVoto(profissionalEncontrado, restauranteEncontrado, votoDTO));
				votacaoEncontrada.getVotos().add(novoVoto);
				votacaoRepository.save(votacaoEncontrada);
				return novoVoto;
			}
			throw new ActionDeniedException("Esse restaurante ja foi escolhido essa semana");
		}
		throw new ActionDeniedException("Profissional j� votou hoje");
	}

	@Override
	public Restaurante apurarVencedor(ProfissionalDTO profissionalDTO, Integer idVotacao) {

		Votacao votacaoEncontrada = localizarVotacao(idVotacao);
		if (votacaoEncontrada.getAtiva()) {
			Profissional profissionalEncontrado = localizarProfissional(profissionalDTO);
			if (profissionalEncontrado.equals(votacaoEncontrada.getFacilitador())) {
				Restaurante vencedor = apurarVotos(votacaoEncontrada);
				votacaoEncontrada.setVencedor(vencedor);
				votacaoEncontrada.setAtiva(Boolean.FALSE);
				votacaoRepository.save(votacaoEncontrada);
				return votacaoEncontrada.getVencedor();
			}
			throw new ActionDeniedException(
					"Profissional n�o � o facilitador. Apenas o faclitador pode apurar o Resultado.");
		}
		throw new ActionDeniedException("Vota��o n�o esta ativa.");
	}

	private Restaurante apurarVotos(Votacao votacao) {

		List<Voto> votos = votacao.getVotos();

		Map<Integer, Long> results = votos.stream()
				.collect(Collectors.groupingBy(Voto::getIdRestauranteVoto, TreeMap::new, Collectors.counting()));

		List<ResultadoVotoDTO> resultadoOrdenado = results.entrySet().stream()
				.map(e -> new ResultadoVotoDTO(e.getKey(), Math.toIntExact(e.getValue()))).collect(Collectors.toList())
				.stream().sorted(Comparator.comparing(ResultadoVotoDTO::getQtdVotos).reversed())
				.collect(Collectors.toList());

		Optional<Restaurante> vencedor = restauranteRepository.findById(resultadoOrdenado.get(0).getIdRestaurante());

		return vencedor.get();
	}

	private Profissional localizarProfissional(ProfissionalDTO profissionalDTO) {
		Optional<Profissional> profissional = profissionalRepository.findById(profissionalDTO.getId());

		if (profissional.isPresent()) {
			return profissional.get();
		}
		throw new ObjectNotFoundException("Profissional n�o encontrado.");
	}

	private Votacao localizarVotacao(Integer idVotacao) {

		Optional<Votacao> votacao = votacaoRepository.findById(idVotacao);

		if (votacao.isPresent()) {
			return votacao.get();
		}
		throw new ObjectNotFoundException("Votac�o n�o encontrada.");
	}

	private Voto setParametorNovoVoto(Profissional profissional, Restaurante restauranteVotado, VotoDTO votoDTO) {
		Voto voto = new Voto();
		voto.setId(null);
		voto.setProfissional(profissional);
		voto.setRestaurante(restauranteVotado);
		voto.setDataVoto(HOJE);
		return voto;
	}

	private boolean validarVotoRestaurante(Restaurante restauranteEncontrado) {
		List<Votacao> votacoesVencidas = votacaoRepository.findByVencedor(restauranteEncontrado);

		if (Validator.has(votacoesVencidas)) {
			if (restauranteVenceuEssaSemana(votacoesVencidas)) {
				return false;
			}
		}
		return true;
	}

	private boolean restauranteVenceuEssaSemana(List<Votacao> votacoesVencidas) {
		return votacoesVencidas.stream().filter(
				votacao -> (DateUtil.getSemanaDoAno(votacao.getDataVotacao())) == (DateUtil.getSemanaDoAno(new Date())))
				.findFirst().isPresent();
	}

	private Restaurante localizarRestaurante(RestauranteDTO restauranteDTO) {
		Optional<Restaurante> restaurante = restauranteRepository.findById(restauranteDTO.getId());

		if (restaurante.isPresent()) {
			return restaurante.get();
		}
		throw new ObjectNotFoundException("Votac�o n�o encontrada.");
	}

	private boolean jaVotou(Profissional profissionalEncontrado) {
		return Validator.has(votoRepository.buscarVotoPorProfissionaleData(profissionalEncontrado, HOJE));
	}

}
