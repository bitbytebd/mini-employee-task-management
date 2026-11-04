interface Environment {
  API_ORIGIN?: string;
}

export async function onRequest(context: { request: Request; env: Environment }): Promise<Response> {
  const origin = context.env.API_ORIGIN;
  if (!origin) {
    return new Response('API_ORIGIN is not configured', { status: 503 });
  }

  const incoming = new URL(context.request.url);
  const upstream = new URL(`${incoming.pathname}${incoming.search}`, origin);
  return fetch(new Request(upstream, context.request));
}
