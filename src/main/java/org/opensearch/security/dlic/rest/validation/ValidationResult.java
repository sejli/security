/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 *
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

package org.opensearch.security.dlic.rest.validation;

import org.opensearch.common.CheckedBiConsumer;
import org.opensearch.common.CheckedConsumer;
import org.opensearch.common.CheckedFunction;
import org.opensearch.core.xcontent.ToXContent;
import org.opensearch.core.rest.RestStatus;

import java.io.IOException;
import java.util.Objects;

public class ValidationResult<C> {

    private final RestStatus status;

    private final C content;

    private final ToXContent errorMessage;

    private ValidationResult(final C jsonContent) {
        this(RestStatus.OK, jsonContent, null);
    }

    private ValidationResult(final RestStatus status, final ToXContent errorMessage) {
        this(status, null, errorMessage);
    }

    private ValidationResult(final RestStatus status, final C jsonContent, final ToXContent errorMessage) {
        this.status = status;
        this.content = jsonContent;
        this.errorMessage = errorMessage;
    }

    public static <L> ValidationResult<L> success(final L content) {
        return new ValidationResult<>(content);
    }

    public static <L> ValidationResult<L> error(final RestStatus status, final ToXContent errorMessage) {
        return new ValidationResult<>(status, errorMessage);
    }

    public <L> ValidationResult<L> map(final CheckedFunction<C, ValidationResult<L>, IOException> mapper) throws IOException {
        if (content != null) {
            return Objects.requireNonNull(mapper).apply(content);
        } else {
            return ValidationResult.error(status, errorMessage);
        }
    }

    public void error(final CheckedBiConsumer<RestStatus, ToXContent, IOException> mapper) throws IOException {
        if (errorMessage != null) {
            Objects.requireNonNull(mapper).accept(status, errorMessage);
        }
    }

    public ValidationResult<C> valid(final CheckedConsumer<C, IOException> mapper) throws IOException {
        if (content != null) {
            Objects.requireNonNull(mapper).accept(content);
        }
        return this;
    }

    public RestStatus status() {
        return status;
    }

    public boolean isValid() {
        return errorMessage == null;
    }

    public ToXContent errorMessage() {
        return errorMessage;
    }

}
