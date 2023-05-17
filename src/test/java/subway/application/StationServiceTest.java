package subway.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import subway.dto.StationAddRequest;
import subway.dto.StationAddResponse;

@DisplayNameGeneration(ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
@Transactional
@SpringBootTest
class StationServiceTest {

    @Autowired
    private StationService stationService;

    @Test
    void 역을_생성한다() {
        // given
        StationAddRequest request = new StationAddRequest("강남역");

        // when
        StationAddResponse response = stationService.createStation(request);

        //then
        assertAll(
                () -> assertThat(response.getName()).isEqualTo("강남역"),
                () -> assertThat(response.getId()).isPositive()
        );
    }
}
