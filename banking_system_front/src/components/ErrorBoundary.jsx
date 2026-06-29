import { Component } from 'react';
import { ErrorFallback } from '../pages/ErrorPage';

export default class ErrorBoundary extends Component {
  state = { error: null };

  static getDerivedStateFromError(error) {
    return { error };
  }

  render() {
    if (this.state.error) {
      return (
        <ErrorFallback
          title="Something went wrong"
          message="The page failed while rendering. Please try again from the dashboard."
        />
      );
    }

    return this.props.children;
  }
}
