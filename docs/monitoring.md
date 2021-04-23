# Monitoring

For monitoring Sentry can be used.

## Sentry

[Sentry][1] is an early warning system. For using it you need to [set the environment variable in Luminus][2] for the `SENTRY_DSN` obtained from your sentry.io project.

For catching exceptions we're using [Reitit's exception middleware][4], which wraps the handler in `sentry-error-handler`. This wrapper sends exceptions to Sentry if `SENTRY_DSN` is known otherwise it'll fall back on ordinary logging.

[1]: https://sentry.io/o
[2]: http://www.luminusweb.net/docs/environment.html
[3]: https://sentry.io/settings/mediquest/projects/mq-clj-zorgrank/keys/
[4]: https://metosin.github.io/reitit/ring/default_middleware.html#exception-handling
