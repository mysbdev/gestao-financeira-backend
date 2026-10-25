package br.edu.ifs.finanse.config;

import br.edu.ifs.finanse.entity.*;
import br.edu.ifs.finanse.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Popula dados de exemplo (Maria e João) para facilitar testes manuais.
 *
 * Como o banco agora é persistente (H2 em arquivo — ver application.properties),
 * este seeder só insere os dados na PRIMEIRA vez que a aplicação sobe (quando a
 * tabela usuario está vazia). Nas próximas vezes ele não faz nada, preservando
 * qualquer conta ou transação que você tenha criado pela tela.
 *
 * Isso substitui o antigo data.sql: inserir via JPA/repository deixa o
 * Hibernate gerar os IDs normalmente, sem risco de colisão com IDs
 * auto-gerados depois (o problema que tínhamos com INSERT de ID fixo).
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final TipoMetaRepository tipoMetaRepository;
    private final CategoriaRepository categoriaRepository;
    private final MetaRepository metaRepository;
    private final OnboardingRepository onboardingRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return; // já foi semeado antes — não faz nada
        }

        TipoMeta reserva = tipoMetaRepository.save(TipoMeta.builder().descricao("Reserva de emergência").build());
        tipoMetaRepository.save(TipoMeta.builder().descricao("Quitação de dívida").build());
        tipoMetaRepository.save(TipoMeta.builder().descricao("Compra planejada").build());

        categoriaRepository.save(Categoria.builder().nome("Alimentação").icone("utensils").build());
        categoriaRepository.save(Categoria.builder().nome("Transporte").icone("car").build());
        categoriaRepository.save(Categoria.builder().nome("Lazer").icone("gamepad").build());
        categoriaRepository.save(Categoria.builder().nome("Saúde").icone("heart-pulse").build());
        categoriaRepository.save(Categoria.builder().nome("Moradia").icone("home").build());
        categoriaRepository.save(Categoria.builder().nome("Educação").icone("book").build());

        // Senha para os dois: 123456
        String senha = passwordEncoder.encode("123456");

        // Maria — 78% do limite usado + meta em andamento -> ROTATIVO + POUPANCA + GERAL no RF17
        Usuario maria = usuarioRepository.save(Usuario.builder()
                .nome("Maria Oliveira")
                .email("maria@exemplo.com")
                .senhaHash(senha)
                .dataNascimento(LocalDate.of(1990, 4, 12))
                .rendaMensal(new BigDecimal("3200.00"))
                .limiteCreditoTotal(new BigDecimal("2000.00"))
                .limiteCreditoUtilizado(new BigDecimal("1560.00"))
                .criadoEm(LocalDateTime.now())
                .build());

        metaRepository.save(Meta.builder()
                .usuario(maria)
                .tipoMeta(reserva)
                .nome("Reserva de emergência")
                .valorAlvo(new BigDecimal("5000.00"))
                .valorAtual(new BigDecimal("3200.00"))
                .prazoMeses(12)
                .dataCriacao(LocalDate.now())
                .status(Meta.StatusMeta.ATIVA)
                .build());

        onboardingRepository.save(Onboarding.builder()
                .usuario(maria)
                .concluido(true)
                .concluidoEm(LocalDateTime.now())
                .build());

        // João — sem sinais de rotativo, sem metas, onboarding pendente -> só GERAL no RF17
        Usuario joao = usuarioRepository.save(Usuario.builder()
                .nome("João Souza")
                .email("joao@exemplo.com")
                .senhaHash(senha)
                .dataNascimento(LocalDate.of(1995, 9, 3))
                .rendaMensal(new BigDecimal("4500.00"))
                .limiteCreditoTotal(new BigDecimal("3000.00"))
                .limiteCreditoUtilizado(new BigDecimal("200.00"))
                .criadoEm(LocalDateTime.now())
                .build());

        onboardingRepository.save(Onboarding.builder()
                .usuario(joao)
                .concluido(false)
                .build());
    }
}
