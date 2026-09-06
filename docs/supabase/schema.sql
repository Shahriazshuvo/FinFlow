-- =========================================================
-- FINFLOW SUPABASE BACKEND SETUP
-- Tables + RLS + Policies + Indexes + Triggers + Analytics
-- Idempotent: safe to re-run.
-- =========================================================

-- =========================================================
-- 1. EXTENSION
-- =========================================================
create extension if not exists pgcrypto;

-- =========================================================
-- 2. TABLES
-- =========================================================
create table if not exists public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  full_name text,
  currency_code text not null default 'USD',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.accounts (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  name text not null,
  type text not null check (type in ('cash', 'bank', 'card', 'wallet')),
  opening_balance numeric(12,2) not null default 0,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  deleted_at timestamptz
);

create table if not exists public.categories (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  name text not null,
  type text not null check (type in ('income', 'expense')),
  color text,
  icon text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  deleted_at timestamptz
);

create table if not exists public.transactions (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  account_id uuid not null references public.accounts(id),
  category_id uuid not null references public.categories(id),
  type text not null check (type in ('income', 'expense')),
  amount numeric(12,2) not null check (amount > 0),
  note text,
  transaction_date date not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  deleted_at timestamptz
);

create table if not exists public.budgets (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  category_id uuid not null references public.categories(id),
  amount numeric(12,2) not null check (amount > 0),
  month date not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  deleted_at timestamptz
);

create table if not exists public.goals (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  name text not null,
  target_amount numeric(12,2) not null check (target_amount > 0),
  current_amount numeric(12,2) not null default 0,
  target_date date,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  deleted_at timestamptz
);

-- =========================================================
-- 3. ENABLE RLS
-- =========================================================
alter table public.profiles enable row level security;
alter table public.accounts enable row level security;
alter table public.categories enable row level security;
alter table public.transactions enable row level security;
alter table public.budgets enable row level security;
alter table public.goals enable row level security;

-- =========================================================
-- 4. CLEAN OLD POLICIES (avoids duplicate-policy errors on re-run)
-- =========================================================
drop policy if exists "profiles_select_own" on public.profiles;
drop policy if exists "profiles_insert_own" on public.profiles;
drop policy if exists "profiles_update_own" on public.profiles;
drop policy if exists "profiles_delete_own" on public.profiles;

drop policy if exists "accounts_select_own" on public.accounts;
drop policy if exists "accounts_insert_own" on public.accounts;
drop policy if exists "accounts_update_own" on public.accounts;
drop policy if exists "accounts_delete_own" on public.accounts;

drop policy if exists "categories_select_own" on public.categories;
drop policy if exists "categories_insert_own" on public.categories;
drop policy if exists "categories_update_own" on public.categories;
drop policy if exists "categories_delete_own" on public.categories;

drop policy if exists "transactions_select_own" on public.transactions;
drop policy if exists "transactions_insert_own" on public.transactions;
drop policy if exists "transactions_update_own" on public.transactions;
drop policy if exists "transactions_delete_own" on public.transactions;

drop policy if exists "budgets_select_own" on public.budgets;
drop policy if exists "budgets_insert_own" on public.budgets;
drop policy if exists "budgets_update_own" on public.budgets;
drop policy if exists "budgets_delete_own" on public.budgets;

drop policy if exists "goals_select_own" on public.goals;
drop policy if exists "goals_insert_own" on public.goals;
drop policy if exists "goals_update_own" on public.goals;
drop policy if exists "goals_delete_own" on public.goals;

-- =========================================================
-- 5. RLS POLICIES: PROFILES
-- =========================================================
create policy "profiles_select_own"
on public.profiles for select
to authenticated
using ((select auth.uid()) = id);

create policy "profiles_insert_own"
on public.profiles for insert
to authenticated
with check ((select auth.uid()) = id);

create policy "profiles_update_own"
on public.profiles for update
to authenticated
using ((select auth.uid()) = id)
with check ((select auth.uid()) = id);

create policy "profiles_delete_own"
on public.profiles for delete
to authenticated
using ((select auth.uid()) = id);

-- =========================================================
-- 6. RLS POLICIES: ACCOUNTS
-- =========================================================
create policy "accounts_select_own"
on public.accounts for select
to authenticated
using ((select auth.uid()) = user_id);

create policy "accounts_insert_own"
on public.accounts for insert
to authenticated
with check ((select auth.uid()) = user_id);

create policy "accounts_update_own"
on public.accounts for update
to authenticated
using ((select auth.uid()) = user_id)
with check ((select auth.uid()) = user_id);

create policy "accounts_delete_own"
on public.accounts for delete
to authenticated
using ((select auth.uid()) = user_id);

-- =========================================================
-- 7. RLS POLICIES: CATEGORIES
-- =========================================================
create policy "categories_select_own"
on public.categories for select
to authenticated
using ((select auth.uid()) = user_id);

create policy "categories_insert_own"
on public.categories for insert
to authenticated
with check ((select auth.uid()) = user_id);

create policy "categories_update_own"
on public.categories for update
to authenticated
using ((select auth.uid()) = user_id)
with check ((select auth.uid()) = user_id);

create policy "categories_delete_own"
on public.categories for delete
to authenticated
using ((select auth.uid()) = user_id);

-- =========================================================
-- 8. RLS POLICIES: TRANSACTIONS
-- Extra check: cannot insert against another user's account/category.
-- =========================================================
create policy "transactions_select_own"
on public.transactions for select
to authenticated
using ((select auth.uid()) = user_id);

create policy "transactions_insert_own"
on public.transactions for insert
to authenticated
with check (
  (select auth.uid()) = user_id
  and exists (
    select 1 from public.accounts a
    where a.id = account_id
    and a.user_id = (select auth.uid())
  )
  and exists (
    select 1 from public.categories c
    where c.id = category_id
    and c.user_id = (select auth.uid())
  )
);

create policy "transactions_update_own"
on public.transactions for update
to authenticated
using ((select auth.uid()) = user_id)
with check (
  (select auth.uid()) = user_id
  and exists (
    select 1 from public.accounts a
    where a.id = account_id
    and a.user_id = (select auth.uid())
  )
  and exists (
    select 1 from public.categories c
    where c.id = category_id
    and c.user_id = (select auth.uid())
  )
);

create policy "transactions_delete_own"
on public.transactions for delete
to authenticated
using ((select auth.uid()) = user_id);

-- =========================================================
-- 9. RLS POLICIES: BUDGETS
-- Extra check: budget category must belong to the same user.
-- =========================================================
create policy "budgets_select_own"
on public.budgets for select
to authenticated
using ((select auth.uid()) = user_id);

create policy "budgets_insert_own"
on public.budgets for insert
to authenticated
with check (
  (select auth.uid()) = user_id
  and exists (
    select 1 from public.categories c
    where c.id = category_id
    and c.user_id = (select auth.uid())
  )
);

create policy "budgets_update_own"
on public.budgets for update
to authenticated
using ((select auth.uid()) = user_id)
with check (
  (select auth.uid()) = user_id
  and exists (
    select 1 from public.categories c
    where c.id = category_id
    and c.user_id = (select auth.uid())
  )
);

create policy "budgets_delete_own"
on public.budgets for delete
to authenticated
using ((select auth.uid()) = user_id);

-- =========================================================
-- 10. RLS POLICIES: GOALS
-- =========================================================
create policy "goals_select_own"
on public.goals for select
to authenticated
using ((select auth.uid()) = user_id);

create policy "goals_insert_own"
on public.goals for insert
to authenticated
with check ((select auth.uid()) = user_id);

create policy "goals_update_own"
on public.goals for update
to authenticated
using ((select auth.uid()) = user_id)
with check ((select auth.uid()) = user_id);

create policy "goals_delete_own"
on public.goals for delete
to authenticated
using ((select auth.uid()) = user_id);

-- =========================================================
-- 11. INDEXES
-- =========================================================
create index if not exists accounts_user_id_idx
on public.accounts(user_id);

create index if not exists categories_user_type_idx
on public.categories(user_id, type);

create unique index if not exists categories_user_type_name_unique_idx
on public.categories(user_id, type, lower(name))
where deleted_at is null;

create index if not exists transactions_user_date_idx
on public.transactions(user_id, transaction_date desc)
where deleted_at is null;

create index if not exists transactions_user_category_idx
on public.transactions(user_id, category_id)
where deleted_at is null;

create index if not exists transactions_user_account_idx
on public.transactions(user_id, account_id)
where deleted_at is null;

create index if not exists budgets_user_month_idx
on public.budgets(user_id, month)
where deleted_at is null;

create unique index if not exists budgets_user_category_month_unique_idx
on public.budgets(user_id, category_id, month)
where deleted_at is null;

create index if not exists goals_user_id_idx
on public.goals(user_id)
where deleted_at is null;

-- =========================================================
-- 12. UPDATED_AT TRIGGERS
-- Incremental sync pulls on `updated_at > watermark`, so these are load-bearing.
-- =========================================================
create or replace function public.set_updated_at()
returns trigger
language plpgsql
as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

drop trigger if exists profiles_set_updated_at on public.profiles;
drop trigger if exists accounts_set_updated_at on public.accounts;
drop trigger if exists categories_set_updated_at on public.categories;
drop trigger if exists transactions_set_updated_at on public.transactions;
drop trigger if exists budgets_set_updated_at on public.budgets;
drop trigger if exists goals_set_updated_at on public.goals;

create trigger profiles_set_updated_at
before update on public.profiles
for each row execute function public.set_updated_at();

create trigger accounts_set_updated_at
before update on public.accounts
for each row execute function public.set_updated_at();

create trigger categories_set_updated_at
before update on public.categories
for each row execute function public.set_updated_at();

create trigger transactions_set_updated_at
before update on public.transactions
for each row execute function public.set_updated_at();

create trigger budgets_set_updated_at
before update on public.budgets
for each row execute function public.set_updated_at();

create trigger goals_set_updated_at
before update on public.goals
for each row execute function public.set_updated_at();

-- =========================================================
-- 13. AUTO PROFILE + DEFAULT DATA ON SIGNUP
-- The Android client relies on this: it never inserts a profile and never seeds categories.
-- =========================================================
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
  default_account_id uuid;
begin
  insert into public.profiles (id, full_name, currency_code)
  values (
    new.id,
    coalesce(new.raw_user_meta_data ->> 'full_name', ''),
    'USD'
  );

  insert into public.accounts (user_id, name, type, opening_balance)
  values (new.id, 'Cash', 'cash', 0)
  returning id into default_account_id;

  insert into public.categories (user_id, name, type, color, icon)
  values
    (new.id, 'Salary', 'income', '#16A34A', 'briefcase'),
    (new.id, 'Bonus', 'income', '#22C55E', 'gift'),
    (new.id, 'Food', 'expense', '#EF4444', 'utensils'),
    (new.id, 'Transport', 'expense', '#3B82F6', 'car'),
    (new.id, 'Shopping', 'expense', '#A855F7', 'shopping-bag'),
    (new.id, 'Bills', 'expense', '#F59E0B', 'receipt'),
    (new.id, 'Health', 'expense', '#EC4899', 'heart'),
    (new.id, 'Entertainment', 'expense', '#6366F1', 'film');

  return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;

create trigger on_auth_user_created
after insert on auth.users
for each row execute function public.handle_new_user();

-- =========================================================
-- 14. ANALYTICS VIEWS
-- security_invoker = true so base-table RLS still applies.
-- =========================================================
drop view if exists public.monthly_income_expense;
drop view if exists public.monthly_category_spending;
drop view if exists public.budget_usage;

create view public.monthly_income_expense
with (security_invoker = true)
as
select
  user_id,
  date_trunc('month', transaction_date)::date as month,
  sum(case when type = 'income' then amount else 0 end) as total_income,
  sum(case when type = 'expense' then amount else 0 end) as total_expense,
  sum(case when type = 'income' then amount else -amount end) as net_amount
from public.transactions
where deleted_at is null
group by user_id, date_trunc('month', transaction_date)::date;

create view public.monthly_category_spending
with (security_invoker = true)
as
select
  t.user_id,
  date_trunc('month', t.transaction_date)::date as month,
  t.category_id,
  c.name as category_name,
  c.color as category_color,
  sum(t.amount) as total_amount
from public.transactions t
join public.categories c on c.id = t.category_id
where t.type = 'expense'
  and t.deleted_at is null
  and c.deleted_at is null
group by t.user_id, date_trunc('month', t.transaction_date)::date, t.category_id, c.name, c.color;

create view public.budget_usage
with (security_invoker = true)
as
select
  b.user_id,
  b.id as budget_id,
  b.category_id,
  c.name as category_name,
  b.month,
  b.amount as budget_amount,
  coalesce(sum(t.amount), 0) as spent_amount,
  greatest(b.amount - coalesce(sum(t.amount), 0), 0) as remaining_amount,
  case
    when b.amount > 0 then round((coalesce(sum(t.amount), 0) / b.amount) * 100, 2)
    else 0
  end as usage_percent
from public.budgets b
join public.categories c on c.id = b.category_id
left join public.transactions t
  on t.category_id = b.category_id
  and t.user_id = b.user_id
  and t.type = 'expense'
  and t.deleted_at is null
  and date_trunc('month', t.transaction_date)::date = b.month
where b.deleted_at is null
  and c.deleted_at is null
group by b.user_id, b.id, b.category_id, c.name, b.month, b.amount;

-- =========================================================
-- 15. GRANTS
-- RLS still filters rows; these only open the tables to the authenticated role.
-- =========================================================
grant usage on schema public to authenticated;

grant select, insert, update, delete on public.profiles to authenticated;
grant select, insert, update, delete on public.accounts to authenticated;
grant select, insert, update, delete on public.categories to authenticated;
grant select, insert, update, delete on public.transactions to authenticated;
grant select, insert, update, delete on public.budgets to authenticated;
grant select, insert, update, delete on public.goals to authenticated;

grant select on public.monthly_income_expense to authenticated;
grant select on public.monthly_category_spending to authenticated;
grant select on public.budget_usage to authenticated;