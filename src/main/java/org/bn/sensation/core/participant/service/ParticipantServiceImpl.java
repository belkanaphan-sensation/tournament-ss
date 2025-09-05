package org.bn.sensation.core.participant.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.common.entity.Person;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.participant.repository.ParticipantRepository;
import org.bn.sensation.core.participant.service.dto.CreateParticipantRequest;
import org.bn.sensation.core.participant.service.dto.ParticipantDto;
import org.bn.sensation.core.participant.service.dto.UpdateParticipantRequest;
import org.bn.sensation.core.participant.service.mapper.ParticipantDtoMapper;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.repository.RoundRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ParticipantServiceImpl implements ParticipantService {

    private final ParticipantRepository participantRepository;
    private final ParticipantDtoMapper participantDtoMapper;
    private final ActivityRepository activityRepository;
    private final RoundRepository roundRepository;

    @Override
    public BaseRepository<ParticipantEntity> getRepository() {
        return participantRepository;
    }

    @Override
    public BaseDtoMapper<ParticipantEntity, ParticipantDto> getMapper() {
        return participantDtoMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ParticipantDto> findAll(Pageable pageable) {
        return participantRepository.findAll(pageable).map(participantDtoMapper::toDto);
    }

    @Override
    @Transactional
    public ParticipantDto create(CreateParticipantRequest request) {
        // Validate activity exists
        ActivityEntity activity = null;
        if (request.getActivityId() != null) {
            activity = activityRepository.findById(request.getActivityId())
                    .orElseThrow(() -> new IllegalArgumentException("Activity not found with id: " + request.getActivityId()));
        }

        // Get rounds
        Set<RoundEntity> rounds = Set.of();
        if (request.getRoundIds() != null && !request.getRoundIds().isEmpty()) {
            rounds = request.getRoundIds().stream()
                    .map(roundId -> roundRepository.findById(roundId)
                            .orElseThrow(() -> new IllegalArgumentException("Round not found with id: " + roundId)))
                    .collect(Collectors.toSet());
        }

        // Create participant entity
        ParticipantEntity participant = ParticipantEntity.builder()
                .person(Person.builder()
                        .name(request.getName())
                        .surname(request.getSurname())
                        .secondName(request.getSecondName())
                        .email(request.getEmail())
                        .phoneNumber(request.getPhoneNumber())
                        .build())
                .number(request.getNumber())
                .activity(activity)
                .rounds(rounds)
                .build();

        ParticipantEntity saved = participantRepository.save(participant);
        return participantDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public ParticipantDto update(Long id, UpdateParticipantRequest request) {
        ParticipantEntity participant = participantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found with id: " + id));

        // Update person data
        Person person = participant.getPerson();
        if (person == null) {
            person = Person.builder().build();
        }

        if (request.getName() != null) person.setName(request.getName());
        if (request.getSurname() != null) person.setSurname(request.getSurname());
        if (request.getSecondName() != null) person.setSecondName(request.getSecondName());
        if (request.getEmail() != null) person.setEmail(request.getEmail());
        if (request.getPhoneNumber() != null) person.setPhoneNumber(request.getPhoneNumber());

        participant.setPerson(person);

        // Update other fields
        if (request.getNumber() != null) participant.setNumber(request.getNumber());

        // Update activity
        if (request.getActivityId() != null) {
            ActivityEntity activity = activityRepository.findById(request.getActivityId())
                    .orElseThrow(() -> new IllegalArgumentException("Activity not found with id: " + request.getActivityId()));
            participant.setActivity(activity);
        }

        // Update rounds
        if (request.getRoundIds() != null) {
            Set<RoundEntity> rounds = request.getRoundIds().stream()
                    .map(roundId -> roundRepository.findById(roundId)
                            .orElseThrow(() -> new IllegalArgumentException("Round not found with id: " + roundId)))
                    .collect(Collectors.toSet());
            participant.setRounds(rounds);
        }

        ParticipantEntity saved = participantRepository.save(participant);
        return participantDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!participantRepository.existsById(id)) {
            throw new IllegalArgumentException("Participant not found with id: " + id);
        }
        participantRepository.deleteById(id);
    }
}
