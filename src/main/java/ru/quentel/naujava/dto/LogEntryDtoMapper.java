package ru.quentel.naujava.dto;

import java.util.List;

import org.springframework.stereotype.Component;

import ru.quentel.naujava.entity.LogEntry;
import ru.quentel.naujava.entity.LogFile;

@Component
public class LogEntryDtoMapper {

    public LogEntryDto toDto(LogEntry entry) {
        LogFile file = entry.getLogFile();
        return new LogEntryDto(
                entry.getId(),
                entry.getEventTime(),
                entry.getLevel() == null ? null : entry.getLevel().getName(),
                entry.getMessage(),
                entry.getLineNumber(),
                file == null ? null : file.getFileName(),
                file == null || file.getApplication() == null ? null : file.getApplication().getName());
    }

    public List<LogEntryDto> toDtoList(List<LogEntry> entries) {
        return entries.stream().map(this::toDto).toList();
    }
}