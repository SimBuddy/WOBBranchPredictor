#ifndef BRANCH_WOB_H
#define BRANCH_WOB_H

#include "../bimodal/bimodal.h"
#include "../perceptron/perceptron.h"

#include <array>

struct wob : champsim::modules::branch_predictor {
  bimodal A{nullptr};
  perceptron B{nullptr};

  using branch_predictor::branch_predictor;

  static constexpr std::size_t EC_ENTRIES = 4096;
  std::array<uint8_t, EC_ENTRIES> error_count{};

  bool last_a_prediction = false;
  bool last_b_consulted = false;
  std::size_t last_ec_index = 0;
  bool last_is_conditional = false;

  [[nodiscard]] static std::size_t ec_index(champsim::address ip) { return ip.to<unsigned long>() % EC_ENTRIES; }

  bool predict_branch(champsim::address ip, champsim::address predicted_target, bool always_taken, uint8_t branch_type);
  void last_branch_result(champsim::address ip, champsim::address target, bool taken, uint8_t branch_type);
};

#endif
