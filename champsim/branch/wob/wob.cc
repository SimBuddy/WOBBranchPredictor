#include "wob.h"

static constexpr uint8_t BRANCH_CONDITIONAL = 2;

bool wob::predict_branch(champsim::address ip, champsim::address predicted_target, bool always_taken, uint8_t branch_type)
{
  last_is_conditional = (branch_type == BRANCH_CONDITIONAL);
  last_b_consulted = false;

  const auto raw_a = A.confidence(ip);
  last_a_prediction = raw_a > 1;

  if (last_is_conditional) {
    last_ec_index = ec_index(ip);
    const bool selector_fires = (raw_a == 0) && (error_count[last_ec_index] >= 3);
    if (selector_fires) {
      last_b_consulted = true;
      return B.predict_branch(ip);
    }
  }

  return last_a_prediction;
}

void wob::last_branch_result(champsim::address ip, champsim::address target, bool taken, uint8_t branch_type)
{
  A.last_branch_result(ip, target, taken, branch_type);

  if (!last_is_conditional)
    return;

  if (last_a_prediction != taken && error_count[last_ec_index] < 3)
    ++error_count[last_ec_index];

  if (last_b_consulted) {
    B.last_branch_result(ip, target, taken, branch_type);
  } else {
    B.spec_global_history <<= 1;
    B.spec_global_history.set(0, taken);
    B.global_history <<= 1;
    B.global_history.set(0, taken);
  }
}
