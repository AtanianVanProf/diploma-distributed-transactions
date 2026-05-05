package am.diploma.benchmark.adapter;

import am.diploma.benchmark.dto.PlaceOrderRequest;
import am.diploma.benchmark.model.ProjectType;
import am.diploma.benchmark.model.TransactionResult;
import am.diploma.benchmark.model.ScenarioType;

public interface ProjectAdapter {

    ProjectType getProjectType();

    TransactionResult executeOrder(PlaceOrderRequest request, ScenarioType scenario);

    void reset();
}
