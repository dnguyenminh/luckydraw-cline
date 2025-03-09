package vn.com.fecredit.app.repository.base;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Base repository interface that provides common functionality for all repositories.
 * Extends JpaRepository and JpaSpecificationExecutor to support basic CRUD operations
 * and specification-based querying.
 *
 * @param <T> Entity type
 * @param <ID> Entity ID type
 */
@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {
    // Common repository methods can be added here
}
