# 0011. Split login and sign-up into two destinations

**Status:** Accepted
**Date:** 2026-09-13

## Context

Sign-in and sign-up were one destination. `AuthRouteKey` addressed a single `AuthScreen`, whose
`AuthState.Mode` flag decided whether the name and confirm-password fields rendered, and one
`AuthViewModel` served both. Three places argued for that shape: the `AuthRouteKey` KDoc,
the `AuthState` KDoc, and `APP_SPEC.md` §6 and §13.

The `AuthRouteKey` KDoc named the specific cost of splitting it:

> Splitting the key without splitting the screen would also scope a separate ViewModel to each
> destination, so toggling from sign-in to sign-up would silently discard the email the user had
> already typed.

The product owner asked for the app to launch on a login screen with a sign-up link below it that
navigates to a separate sign-up screen. That is a direct request for the split, so the question
became whether the original objection still holds.

It largely does not. The objection assumes a *toggle* — a control the user flips back and forth
in place, where losing typed input is a visible regression. A one-way link from sign-in to sign-up
is a different interaction: the user following it does not have an account, so there is rarely a
typed email worth carrying.

The merged screen also had two costs that were not written down. `AuthState` carried
`displayName` and `confirmPassword` on every sign-in, fields that mode could not use; and errors
were a single form-level `errorMessage`, because one screen serving two forms cannot easily say
which of up to four inputs a message belongs to.

## Decision

Two destinations — `LoginRouteKey` (the auth graph's start) and `SignUpRouteKey` — with a
contract, screen and ViewModel each, and no mode flag anywhere.

The split goes all the way down rather than stopping at the key. A shared ViewModel behind two
route keys would have been the worst of both: two destinations that still carry each other's
fields.

Errors became per-field as a direct consequence. Each ViewModel runs `AuthValidator` per input
before submitting, purely to decide *placement* — the use case runs the same rules and remains
the authority, but it short-circuits on the first failure and returns one message with no
indication of which box it belongs to. No rule is restated: the email regex and the password
minimum still live only in `AuthValidator`.

## Consequences

An email typed on one screen does not survive the trip to the other. If that ever matters,
`SignUpRouteKey` takes a `prefillEmail: String?` — the fix is a nav argument, not a re-merge.

Duplication between the two contracts is real and accepted: both carry an email, a password, an
`isSubmitting` and a form error. They are separated because the two forms diverge in everything
else, and because a shared base state would reintroduce exactly the "fields the screen cannot
use" problem the split removes.

`AuthValidator`'s blank-field rules ("Enter your name", "Enter your email", "Enter your password")
are now unreachable from these screens: `canSubmit` already refuses a form with an empty box, so
the disabled button is the feedback. They still apply to any other entry point, and they start
firing here the moment one gains real content. `SignUpViewModelTest` pins this so it is not
rediscovered as a bug.

Two navigation entry points exist where there was one, so `authScreens()` takes four lambdas
rather than two. The app still owns graph composition (§6) — the feature has no `NavController`.

## Alternatives considered

**Keep the merged screen and restyle it.** The original decision, and defensible. Rejected because
the request was explicit, and because re-reading the objection showed it was written against a
toggle rather than against a one-way link.

**Split the route key but keep one screen and ViewModel.** Rejected: it gives two URLs to the same
form, which is worse than either end state — the fields the mode cannot use remain, and the back
stack now contains a destination that renders identically to the one below it.

**Share a base `AuthFormState` between the two contracts.** Rejected. The shared part is three
primitives; the cost of a shared supertype is that every future field has to be argued about
twice, and that a change for one screen silently reaches the other.