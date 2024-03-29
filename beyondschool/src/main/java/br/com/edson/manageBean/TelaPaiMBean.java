package br.com.edson.manageBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.servlet.http.HttpSession;
import javax.swing.JOptionPane;

import org.hibernate.Session;

import br.com.edson.Model.Aluno;
import br.com.edson.Model.Avaliacao;
import br.com.edson.Model.Comentario;
import br.com.edson.Model.Turma;
import br.com.edson.Model.Usuario;
import br.com.edson.repository.AlunosBD;
import br.com.edson.repository.AvaliacoesBD;
import br.com.edson.repository.ComentariosBD;
import br.com.edson.repository.TurmasBD;
import br.com.edson.repository.UsuariosBD;
import br.com.edson.service.BuscaDadosResponsavel;
import br.com.edson.service.NegocioException;

@Named
@javax.faces.view.ViewScoped
public class TelaPaiMBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Inject
	private Turma turma;
	
	@Inject
	private TurmasBD turmasBD;
	
	@Inject
	private Usuario user;
		
	@Inject
	private Avaliacao avaliacao;
	
	private List<Avaliacao> avaliacoes = new ArrayList<Avaliacao>();
	
	@Inject
	private AvaliacoesBD avaliacoesBD;
	
	@Inject
	private Aluno aluno;
	
	private List<Aluno> alunos = new ArrayList<Aluno>();
	
	@Inject
	private AlunosBD alunosBD;
	
	private List<String> comentarios = new ArrayList<String>();
	
	private String[] comentario = new String[2];
	
	private String comentarioResponsavel;
	
	private boolean flagCommentAluno = false;
	
	private boolean flagCommentPai = false;
	
	private boolean flagTemAvaliacao = true;
	
	
	
	@Inject
	private BuscaDadosResponsavel buscaResp;
	
	@Inject
	private Comentario objComentario;
	
	HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
	
	@Inject
	private EntityManager em;
	
	@Inject
	private ComentariosBD comentariosBD;
	
	private boolean nextAva = false;
	
	private List<Comentario> comments = new ArrayList<Comentario>();
	
	@Inject
	private ComentariosBD commentsBD;

	//métodos
	// fazer lógica para exibir os comentarios
	
	public void buscarAlunos() throws NegocioException {
		user = (Usuario) session.getAttribute("usuario");
		alunos = buscaResp.buscaAlunosDoResponsavel(user.getPessoa().getIdPessoa());
		
	}
	
	public void buscarTurma() {
		turma = turmasBD.buscaTurma(alunos.get(0).getIdPessoa());
		aluno = alunos.get(0);
		avaliacao = avaliacoesBD.buscaPorIdAluno(aluno.getIdPessoa());
		if(avaliacao == null) {
			flagTemAvaliacao = false;
		}
	}
	
	public void proximoAluno() {
		flagCommentAluno = false;
		flagCommentPai = false;
		alunos.remove(aluno);
		buscarTurma();
		buscarComentarios();
		if( flagTemAvaliacao) {
			if( avaliacao.getComentarios().size() == 1) {
				flagCommentAluno = false;
				flagCommentPai = false;
			}
		}

			
	}
	
	public void buscarComentarios() {
		if( flagTemAvaliacao ) {
			
			
			if( avaliacao.getComentarios().size() > 1 && avaliacao.getComentarios().get(1).getIdPessoaQueFez().equals(aluno.getIdPessoa())  )
			{	
				flagCommentAluno = true;
				comentario[0] = avaliacao.getComentarios().get(1).getComentario();
			}
			else if ( avaliacao.getComentarios().size() > 1  ){
				
				flagCommentPai = true;
				comentario[1] = avaliacao.getComentarios().get(1).getComentario();
			}
		}
		if( flagTemAvaliacao && avaliacao.getComentarios().size() > 2   ) {
			if( avaliacao.getComentarios().get(1).getIdPessoaQueFez().equals(aluno.getIdPessoa()) )
			{	
				flagCommentPai = true;
				flagCommentAluno = true;
				comentario[0] = avaliacao.getComentarios().get(1).getComentario();
				comentario[1] = avaliacao.getComentarios().get(2).getComentario();
			}
			else { 
				flagCommentPai = true;
				flagCommentAluno = true;
				comentario[0] = avaliacao.getComentarios().get(2).getComentario();
				comentario[1] = avaliacao.getComentarios().get(1).getComentario();
			}
		}
			
	}
	
	public void salvarComentarioResponsavel() throws NegocioException {
		FacesContext context = FacesContext.getCurrentInstance();
		EntityTransaction et = this.em.getTransaction();
		
		try {
			
			objComentario.setComentario(comentarioResponsavel);
			objComentario.setDataComentario(new Date());
			objComentario.setIdPessoaQueFez(user.getPessoa().getIdPessoa());
			objComentario.setAvaliacao(avaliacao);
			
			et.begin();
			
			
			comentariosBD.salvarComentario(objComentario);
			
			et.commit();
			flagCommentPai = true;
			context.addMessage(null, new FacesMessage("Comentário salvo."));
			buscarComentarios();
		} catch (PersistenceException |NegocioException e) {
			et.rollback();
			e.printStackTrace();
			FacesMessage msg = new FacesMessage("Falha ao salvar comentário");
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			context.addMessage(null, msg);
		}
	}
	
	public void next()  {
		avaliacoes.remove(0);
		
		if(avaliacoes.size() == 1 ) {
			nextAva = false;
			flagTemAvaliacao = true;
			comments = commentsBD.porIdAvaliacao(avaliacoes.get(0).getIdAvaliacao());
			avaliacoes.get(0).setComentarios(comments);
		
			avaliacao = avaliacoes.get(0);
			FacesContext context = FacesContext.getCurrentInstance();
			context.addMessage(null, new FacesMessage("Esta é a última avaliação"));
		}
		else if( avaliacoes.size() > 1) {
			nextAva = true;
			flagTemAvaliacao = true;
			comments = commentsBD.porIdAvaliacao(avaliacoes.get(0).getIdAvaliacao());
			avaliacoes.get(0).setComentarios(comments);
			avaliacao = avaliacoes.get(0);
		}
					
	}
	
	public void todasAvaliacoes() throws NegocioException {
		
		avaliacoes = avaliacoesBD.todasAvaliacoesAluno(aluno.getIdPessoa());
		
		if (avaliacoes == null) {
			throw new NegocioException("Falha ao buscar avaliações.");
		}
		if( avaliacoes.size() == 1) {
			FacesContext context = FacesContext.getCurrentInstance();
			context.addMessage(null, new FacesMessage("Esta é a única avaliação"));
		}
		else {
			nextAva = true;
			avaliacao = avaliacoes.get(0);
		}
	}
	
	//getters and setters 
	public Turma getTurma() {
		return turma;
	}

	public void setTurma(Turma turma) {
		this.turma = turma;
	}

	public Usuario getUser() {
		return user;
	}

	public void setUser(Usuario user) {
		this.user = user;
	}

	public Avaliacao getAvaliacao() {
		return avaliacao;
	}

	public void setAvaliacao(Avaliacao avaliacao) {
		this.avaliacao = avaliacao;
	}

	public Aluno getAluno() {
		return aluno;
	}

	public void setAluno(Aluno aluno) {
		this.aluno = aluno;
	}

	public List<String> getComentarios() {
		return comentarios;
	}

	public void setComentarios(List<String> comentarios) {
		this.comentarios = comentarios;
	}

	public String[] getComentario() {
		return comentario;
	}

	public void setComentario(String[] comentario) {
		this.comentario = comentario;
	}

	public boolean isFlagCommentAluno() {
		return flagCommentAluno;
	}

	public void setFlagCommentAluno(boolean flagCommentAluno) {
		this.flagCommentAluno = flagCommentAluno;
	}

	public boolean isFlagCommentPai() {
		return flagCommentPai;
	}

	public void setFlagCommentPai(boolean flagCommentPai) {
		this.flagCommentPai = flagCommentPai;
	}

	public List<Aluno> getAlunos() {
		return alunos;
	}

	public void setAlunos(List<Aluno> alunos) {
		this.alunos = alunos;
	}

	public String getComentarioResponsavel() {
		return comentarioResponsavel;
	}

	public void setComentarioResponsavel(String comentarioResponsavel) {
		this.comentarioResponsavel = comentarioResponsavel;
	}

	public boolean isFlagTemAvaliacao() {
		return flagTemAvaliacao;
	}

	public void setFlagTemAvaliacao(boolean flagTemAvaliacao) {
		this.flagTemAvaliacao = flagTemAvaliacao;
	}

	public boolean isNextAva() {
		return nextAva;
	}

	public void setNextAva(boolean nextAva) {
		this.nextAva = nextAva;
	}

	
	
	
	
}
