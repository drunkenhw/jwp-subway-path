package subway.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import subway.domain.Line;
import subway.domain.Section;
import subway.dto.LineRequest;
import subway.dto.LineResponse;
import subway.dto.LineSaveResponse;
import subway.dto.SectionAddRequest;
import subway.dto.StationDeleteRequest;
import subway.dto.StationResponse;
import subway.exception.DuplicatedNameException;
import subway.repository.LineRepository;

@DisplayNameGeneration(ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
@Transactional
@SpringBootTest
class LineServiceTest {

    @Autowired
    private LineService lineService;
    @Autowired
    private LineRepository lineRepository;

    @Test
    void 노선을_추가한다() {
        //given
        LineRequest request = new LineRequest("2호선", "A", "B", 10);

        //when
        LineSaveResponse saveResponse = lineService.saveLine(request);

        //then
        assertAll(
                () -> assertThat(lineRepository.findAll()).hasSize(1),
                () -> assertThat(saveResponse.getId()).isPositive()
        );
    }

    @Test
    void 이미_존재하는_이름의_노선을_추가하면_예외가_발생한다() {
        // given
        LineRequest request = new LineRequest("2호선", "A", "B", 10);
        lineService.saveLine(request);

        // when
        LineRequest secondRequest = new LineRequest("2호선", "B", "C", 10);

        // then
        assertThatThrownBy(() -> lineService.saveLine(secondRequest))
                .isInstanceOf(DuplicatedNameException.class)
                .hasMessage("이미 존재하는 이름입니다 입력값 : " + secondRequest.getLineName());
    }

    @Test
    void 노선에_역을_추가한다() {
        //given
        lineRepository.save(new Line("1호선", List.of(
                new Section("A", "B", 2)
        )));
        Long savedLineId = lineRepository.save(new Line("2호선", List.of(
                new Section("X", "B", 2),
                new Section("B", "Y", 3)
        )));

        //when
        lineService.addStation(savedLineId, new SectionAddRequest("Y", "Z", 10));

        //then
        assertThat(lineRepository.findAll()).flatExtracting(Line::getSections)
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .isEqualTo(List.of(
                        new Section("A", "B", 2),
                        new Section("X", "B", 2),
                        new Section("B", "Y", 3),
                        new Section("Y", "Z", 10)
                ));
    }

    @Test
    void 노선에_역을_삭제한다() {
        //given
        lineRepository.save(new Line("1호선", List.of(
                new Section("A", "B", 2)
        )));
        Long savedLineId = lineRepository.save(new Line("2호선", List.of(
                new Section("X", "B", 2),
                new Section("B", "Y", 3)
        )));

        //when
        lineService.deleteStation(savedLineId, new StationDeleteRequest("Y"));

        //then
        assertThat(lineRepository.findAll()).flatExtracting(Line::getSections)
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .isEqualTo(List.of(
                        new Section("A", "B", 2),
                        new Section("X", "B", 2)
                ));
    }

    @Test
    void 노선에_역이_하나_남으면_전체를_삭제한다() {
        //given
        Long savedLineId = lineRepository.save(new Line("1호선", List.of(
                new Section("A", "B", 2)
        )));

        //when
        lineService.deleteStation(savedLineId, new StationDeleteRequest("A"));

        //then
        assertThat(lineRepository.findAll()).flatExtracting(Line::getStations)
                .hasSize(0);
    }

    @Test
    void 전체_노선을_역과_함께_조회한다() {
        //given
        lineRepository.save(new Line("1호선", List.of(
                new Section("A", "B", 2)
        )));
        lineRepository.save(new Line("2호선", List.of(
                new Section("X", "B", 2),
                new Section("B", "Y", 3)
        )));

        //when
        List<LineResponse> lines = lineService.findAllLines();

        //then
        assertAll(
                () -> assertThat(lines).hasSize(2),
                () -> assertThat(lines).flatExtracting(LineResponse::getStations)
                        .map(StationResponse::getName)
                        .contains("A", "B", "X", "Y")
        );
    }

    @Test
    void 단일_노선을_조회한다() {
        //given
        lineRepository.save(new Line("1호선", List.of(
                new Section("A", "B", 2)
        )));
        Long savedLineId = lineRepository.save(new Line("2호선", List.of(
                new Section("X", "B", 2),
                new Section("B", "Y", 3)
        )));

        //when
        LineResponse line = lineService.findLineById(savedLineId);

        //then
        assertThat(line.getStations())
                .map(StationResponse::getName)
                .contains("B", "X", "Y");
    }
}
