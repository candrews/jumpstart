import Cookies from 'js-cookie';

export const apiClient = async (input: RequestInfo, { headers, ...init }: RequestInit = {}): Promise<Response> => {
  const csrfToken = Cookies.get('XSRF-TOKEN');
  const response: Response = await fetch(
    input,
    { ...init, headers: csrfToken ? { 'X-XSRF-TOKEN': csrfToken, ...headers } : headers },
  );
  if (response.status >= 400) throw response;
  return response;
};
