-- First delete all history/transaction tables
DELETE FROM public.spin_histories;

-- Then delete junction/mapping tables
DELETE FROM public.participant_events;
DELETE FROM public.participant_roles;
DELETE FROM public.user_roles;

-- Then delete entity tables with foreign keys
DELETE FROM public.golden_hours;
DELETE FROM public.rewards;
DELETE FROM public.participants;
DELETE FROM public.event_locations;
DELETE FROM public.events;
DELETE FROM public.provinces;
DELETE FROM public.regions;

-- Finally delete independent tables
DELETE FROM public.roles;
DELETE FROM public.users;
