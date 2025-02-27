package vn.com.fecredit.app.util;

import vn.com.fecredit.app.entity.*;

/**
 * Helper class to handle entity relationship setting in tests
 */
public class TestEntitySetter {

    public static void setSpinLocation(SpinHistory spin, EventLocation location) {
        try {
            spin.getClass().getMethod("setLocation", EventLocation.class).invoke(spin, location);
        } catch (Exception e1) {
            try {
                spin.getClass().getMethod("setEventLocation", EventLocation.class).invoke(spin, location);
            } catch (Exception e2) {
                // Try direct field access as last resort
                try {
                    var field = spin.getClass().getDeclaredField("location");
                    field.setAccessible(true);
                    field.set(spin, location);
                } catch (Exception e3) {
                    throw new RuntimeException("Unable to set location on SpinHistory", e3);
                }
            }
        }
    }

    public static void setSpinReward(SpinHistory spin, Reward reward) {
        try {
            spin.setReward(reward);
        } catch (Exception e) {
            try {
                var field = spin.getClass().getDeclaredField("reward");
                field.setAccessible(true);
                field.set(spin, reward);
            } catch (Exception e2) {
                throw new RuntimeException("Unable to set reward on SpinHistory", e2);
            }
        }
    }

    public static void setSpinEvent(SpinHistory spin, Event event) {
        try {
            spin.setEvent(event);
        } catch (Exception e) {
            try {
                var field = spin.getClass().getDeclaredField("event");
                field.setAccessible(true);
                field.set(spin, event);
            } catch (Exception e2) {
                throw new RuntimeException("Unable to set event on SpinHistory", e2);
            }
        }
    }

    public static void setSpinParticipant(SpinHistory spin, Participant participant) {
        try {
            spin.setParticipant(participant);
        } catch (Exception e) {
            try {
                var field = spin.getClass().getDeclaredField("participant");
                field.setAccessible(true);
                field.set(spin, participant);
            } catch (Exception e2) {
                throw new RuntimeException("Unable to set participant on SpinHistory", e2);
            }
        }
    }

    public static void setLocationEvent(EventLocation location, Event event) {
        try {
            location.setEvent(event);
        } catch (Exception e) {
            try {
                var field = location.getClass().getDeclaredField("event");
                field.setAccessible(true);
                field.set(location, event);
            } catch (Exception e2) {
                throw new RuntimeException("Unable to set event on EventLocation", e2);
            }
        }
    }

    public static void setRewardEvent(Reward reward, Event event) {
        try {
            reward.setEvent(event);
        } catch (Exception e) {
            try {
                var field = reward.getClass().getDeclaredField("event");
                field.setAccessible(true);
                field.set(reward, event);
            } catch (Exception e2) {
                throw new RuntimeException("Unable to set event on Reward", e2);
            }
        }
    }

    public static void setParticipantEvent(Participant participant, Event event) {
        try {
            participant.setEvent(event);
        } catch (Exception e) {
            try {
                var field = participant.getClass().getDeclaredField("event");
                field.setAccessible(true);
                field.set(participant, event);
            } catch (Exception e2) {
                throw new RuntimeException("Unable to set event on Participant", e2);
            }
        }
    }
}
