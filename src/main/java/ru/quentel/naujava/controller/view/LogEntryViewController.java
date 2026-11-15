package ru.quentel.naujava.controller.view;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ru.quentel.naujava.criteria.LogEntryCriteriaRepository;
import ru.quentel.naujava.dto.LogEntryDtoMapper;
import ru.quentel.naujava.entity.LogEntry;
import ru.quentel.naujava.repository.LogEntryRepository;
import ru.quentel.naujava.service.LogEntryService;

/**
 * HTML-представление записей логов: список, статистика и поиск кастомными методами.
 */
@Controller
@RequestMapping("/log-entries/view")
public class LogEntryViewController {

    private final LogEntryRepository logEntryRepository;
    private final LogEntryCriteriaRepository logEntryCriteriaRepository;
    private final LogEntryService logEntryService;
    private final LogEntryDtoMapper mapper;

    @Autowired
    public LogEntryViewController(LogEntryRepository logEntryRepository,
                                  LogEntryCriteriaRepository logEntryCriteriaRepository,
                                  LogEntryService logEntryService,
                                  LogEntryDtoMapper mapper) {
        this.logEntryRepository = logEntryRepository;
        this.logEntryCriteriaRepository = logEntryCriteriaRepository;
        this.logEntryService = logEntryService;
        this.mapper = mapper;
    }

    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("entries", mapper.toDtoList(logEntryRepository.findAll()));
        model.addAttribute("title", "Все записи логов");
        fillStats(model);
        fillForm(model, null, null, null, null, false);
        return "logEntryList";
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String level,
                         @RequestParam(required = false)
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                         @RequestParam(required = false)
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
                         @RequestParam(required = false) String applicationName,
                         @RequestParam(defaultValue = "false") boolean useCriteria,
                         Model model) {

        List<LogEntry> found;
        String title;

        if (applicationName != null && !applicationName.isBlank()) {
            String name = applicationName.trim();
            found = useCriteria
                    ? logEntryCriteriaRepository.findByApplicationName(name)
                    : logEntryRepository.findByApplicationName(name);
            title = "Записи логов приложения: " + name;
        } else if (level != null && !level.isBlank()) {
            String levelName = level.trim().toUpperCase(Locale.ROOT);
            LocalDateTime start = from != null ? from : LocalDateTime.now().minusDays(30);
            LocalDateTime end = to != null ? to : LocalDateTime.now();
            found = useCriteria
                    ? logEntryCriteriaRepository.findByLevelNameAndEventTimeBetween(levelName, start, end)
                    : logEntryRepository.findByLevelNameAndEventTimeBetween(levelName, start, end);
            title = "Записи уровня " + levelName + " за период " + start + " — " + end;
        } else {
            found = logEntryRepository.findAll();
            title = "Все записи логов (фильтр не задан)";
        }

        model.addAttribute("entries", mapper.toDtoList(found));
        model.addAttribute("title", title + (useCriteria ? " [Criteria API]" : " [Spring Data]"));
        fillStats(model);
        fillForm(model, level, from, to, applicationName, useCriteria);
        return "logEntryList";
    }

    private void fillStats(Model model) {
        model.addAttribute("levelStats", logEntryService.countByLevel());
        model.addAttribute("topMessages", logEntryService.topFrequentMessages(5));
    }

    private void fillForm(Model model, String level, LocalDateTime from, LocalDateTime to,
                          String applicationName, boolean useCriteria) {
        model.addAttribute("level", level == null ? "" : level);
        model.addAttribute("from", from == null ? "" : from.toString());
        model.addAttribute("to", to == null ? "" : to.toString());
        model.addAttribute("applicationName", applicationName == null ? "" : applicationName);
        model.addAttribute("useCriteria", useCriteria);
    }
    @ModelAttribute
    public void addCurrentUser(Model model, Authentication authentication) {
        if (authentication != null) {
            model.addAttribute("currentUsername", authentication.getName());
            model.addAttribute("isAdmin", authentication.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())));
        }
    }
}