package br.com.edson.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.swing.JOptionPane;

import br.com.edson.Model.Aluno;
import br.com.edson.Model.Responsavel;
import br.com.edson.Model.Usuario;
import br.com.edson.service.NegocioException;

public class ResponsaveisBD implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Inject
	private EntityManager em;

	@Inject
	private Usuario user;
	
	@Inject
	private UsuariosBD usersBD;
	
	@Inject
	public ResponsaveisBD() {
	}
	
	public List<Responsavel> buscaTodos(){
		TypedQuery<Responsavel> responsaveis = em.createQuery("from Responsavel ", Responsavel.class);
		return responsaveis.getResultList();
	}
	
	public Responsavel buscaResponsavelPeloNome(String nome) {
		Responsavel resp;
		try {
			String sql = "select r from Responsavel r where nomeCompleto = :nomeCompleto";
			resp = this.em.createQuery(sql, Responsavel.class).setParameter("nomeCompleto", nome).getSingleResult();
		} catch (PersistenceException e) {
			return null;
		}
		return resp;
	}
	
	/**
	 * deleta do banco de dados
	 * @param idResponsavelSerExcluido
	 * @throws NegocioException
	 * @throws Exception
	 */
	public void removeResponsavel( Long idResponsavelSerExcluido) throws NegocioException, Exception {
		Responsavel resp = this.em.find(Responsavel.class, idResponsavelSerExcluido);
		usersBD.excluirUser(resp.getIdPessoa());
		this.em.remove(resp);
	}
	
	/**
	 * update do responsavel, o atributo deletado passa para true
	 * @param idResponsavelSerExcluido
	 * @throws NegocioException
	 * @throws Exception
	 */
	public void excluirResponsavel( Long idResponsavelSerExcluido) throws NegocioException, Exception {
		Responsavel resp = this.em.find(Responsavel.class, idResponsavelSerExcluido);
		resp.setDeletado(true);
		try {
			this.em.merge(resp);
		} catch (Exception e) {
			e.printStackTrace();
			throw new NegocioException("Falha ao excluir responsável");
		}
		
		
	}
	
	
	public void salvarResponsavelCadastro(Responsavel responsavel) throws PersistenceException {
		this.em.persist(responsavel);		
	}

	public Responsavel porId(Long idPessoa) {
		try {
			return this.em.find(Responsavel.class,idPessoa);
		} catch (Exception e) {
			return null;
		}
		
	}	

}
