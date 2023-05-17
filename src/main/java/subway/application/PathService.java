package subway.application;

import org.springframework.stereotype.Service;
import subway.application.charge.ChargePolicy;
import subway.domain.Station;
import subway.domain.Subway;
import subway.domain.path.ShortestPath;
import subway.domain.path.SubwayGraph;
import subway.dto.PathRequest;
import subway.dto.PathResponse;
import subway.repository.LineRepository;

@Service
public class PathService {

    private final LineRepository lineRepository;
    private final ChargePolicy chargePolicy;

    public PathService(LineRepository lineRepository, ChargePolicy chargePolicy) {
        this.lineRepository = lineRepository;
        this.chargePolicy = chargePolicy;
    }

    public PathResponse findShortestPath(PathRequest request) {
        Station source = lineRepository.findStationByStationName(request.getSource());
        Station target = lineRepository.findStationByStationName(request.getTarget());
        validatePath(source, target);
        SubwayGraph graph = SubwayGraph.from(new Subway(lineRepository.findAll()));
        ShortestPath path = graph.findPath(source, target);
        int charge = chargePolicy.calculateFee(path.getDistance());
        return PathResponse.of(path, charge);
    }

    private void validatePath(Station source, Station target) {
        if (source.equals(target)) {
            throw new IllegalArgumentException("출발지와 도착지는 같을 수 없습니다.");
        }
    }
}
