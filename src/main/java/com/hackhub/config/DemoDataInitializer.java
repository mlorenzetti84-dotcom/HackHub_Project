package com.hackhub.config;

import com.hackhub.domain.actor.Judge;
import com.hackhub.domain.actor.Mentor;
import com.hackhub.domain.actor.Organizer;
import com.hackhub.domain.actor.Team;
import com.hackhub.domain.actor.User;
import com.hackhub.domain.hackathon.Hackathon;
import com.hackhub.domain.hackathon.HackathonStatus;
import com.hackhub.repository.HackathonRepository;
import com.hackhub.repository.TeamRepository;
import com.hackhub.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DemoDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final HackathonRepository hackathonRepository;

    public DemoDataInitializer(
            UserRepository userRepository,
            TeamRepository teamRepository,
            HackathonRepository hackathonRepository
    ) {
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.hackathonRepository = hackathonRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (hackathonRepository.count() > 0) {
            return;
        }

        Organizer organizer = userRepository.save(
                new Organizer("Prof. Andrea Rossi - Organizzatore", "a.rossi@unicam.it")
        );
        Judge judge = userRepository.save(
                new Judge("Elena Verdi - Tech Lead", "e.verdi@tech.it")
        );
        Mentor devOpsMentor = userRepository.save(
                new Mentor("Marco Neri (DevOps)", "m.neri@hackhub.it")
        );
        Mentor aiMentor = userRepository.save(
                new Mentor("Giulia Gialli (AI Specialist)", "g.gialli@hackhub.it")
        );

        User luca = userRepository.save(new User("Luca Ferri", "luca.ferri@studenti.unicam.it"));
        User sofia = userRepository.save(new User("Sofia Conti", "sofia.conti@studenti.unicam.it"));
        User matteo = userRepository.save(new User("Matteo Ricci", "matteo.ricci@studenti.unicam.it"));
        User chiara = userRepository.save(new User("Chiara Romano", "chiara.romano@studenti.unicam.it"));
        User davide = userRepository.save(new User("Davide Moretti", "davide.moretti@studenti.unicam.it"));
        User alessandro = userRepository.save(new User("Alessandro Riva", "alessandro.riva@studenti.unicam.it"));

        Team nullPointerSyndicate = new Team("Team CyberSecurity", luca, 3);
        nullPointerSyndicate.addMember(sofia);
        nullPointerSyndicate.addMember(matteo);
        nullPointerSyndicate = teamRepository.save(nullPointerSyndicate);

        Team caffeinatedCoders = new Team("Caffeinated Coders", chiara, 3);
        caffeinatedCoders.addMember(davide);
        caffeinatedCoders = teamRepository.save(caffeinatedCoders);

        Team greenStack = teamRepository.save(new Team("Green Stack Collective", alessandro, 3));
        LocalDate today = LocalDate.now();

        Hackathon hackathon = new Hackathon(
                "CodeFest Camerino 2026: Sostenibilita Digitale",
                "Polo Informatico - Laboratorio Alan Turing, Camerino",
                """
                Premio: 2500 Euro e Stage Aziendale.

                Art 1. E consentito l'uso di librerie open-source, purche correttamente dichiarate nel README del progetto.
                Art 2. Tutto il codice deve essere caricato su GitHub entro le 48 ore dalla partenza ufficiale dell'hackathon.
                Art 3. I team sorpresi a usare codice pre-scritto per le logiche core verranno squalificati.
                Art 4. Ogni team deve consegnare un prototipo eseguibile, una breve relazione tecnica e una demo funzionale.
                Art 5. La valutazione considera impatto ambientale, qualita architetturale, usabilita e sostenibilita del modello operativo.
                """,
                today.minusDays(7),
                today.minusDays(1),
                today,
                today.plusDays(2),
                today.plusDays(5),
                BigDecimal.valueOf(2500),
                3,
                organizer
        );

        hackathon.addStaffMember(organizer, judge);
        hackathon.addMentor(devOpsMentor);
        hackathon.addMentor(aiMentor);

        hackathon.registerTeam(nullPointerSyndicate);
        hackathon.registerTeam(caffeinatedCoders);
        hackathon.registerTeam(greenStack);

        hackathon.advanceTo(HackathonStatus.IN_CORSO);

        hackathon.submitProject(
                nullPointerSyndicate,
                "Algoritmo AI per l'ambiente",
                "https://github.com/team-cybersecurity/green-ai-routing - Motore predittivo che ottimizza consumi energetici e percorsi logistici a basso impatto."
        );

        hackathon.submitProject(
                caffeinatedCoders,
                "EnergyPulse",
                "github.com/caffeinated-coders/energypulse - Dashboard per monitorare consumi energetici degli edifici universitari."
        );

        hackathon.reportViolation(
                devOpsMentor,
                greenStack,
                "Sospetto utilizzo di codice non autorizzato durante la notte: repository locale con moduli core gia completi prima dell'avvio."
        );

        hackathonRepository.save(hackathon);

        System.out.println("Demo data ready for CodeFest Camerino 2026:");
        System.out.println("- Organizer ID: " + organizer.getId() + " | " + organizer.getUsername());
        System.out.println("- Judge ID: " + judge.getId() + " | " + judge.getUsername());
        System.out.println("- Mentor ID: " + devOpsMentor.getId() + " | " + devOpsMentor.getUsername());
        System.out.println("- Mentor ID: " + aiMentor.getId() + " | " + aiMentor.getUsername());
        System.out.println("- Team ID: " + nullPointerSyndicate.getId() + " | " + nullPointerSyndicate.getName());
        System.out.println("- Team ID: " + caffeinatedCoders.getId() + " | " + caffeinatedCoders.getName());
        System.out.println("- Team ID: " + greenStack.getId() + " | " + greenStack.getName());
    }
}
