package com.chutneytesting.admin.api;

import com.chutneytesting.admin.domain.DatabaseAdminService;
import com.chutneytesting.admin.domain.SqlResult;
import com.chutneytesting.tools.PaginatedDto;
import com.chutneytesting.tools.PaginationRequestWrapperDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/database")
@CrossOrigin(origins = "*")
public class DatabaseManagementController {

    private final DatabaseAdminService jdbcAdminService;
    private final DatabaseAdminService orientAdminService;

    DatabaseManagementController(DatabaseAdminService jdbcAdminService, DatabaseAdminService orientAdminService) {
        this.jdbcAdminService = jdbcAdminService;
        this.orientAdminService = orientAdminService;
    }

    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @PostMapping("/execute/orient")
    public SqlResult executeOrient(@RequestBody String query) {
        return orientAdminService.execute(query);
    }

    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @PostMapping("/execute/jdbc")
    public SqlResult executeh2(@RequestBody String query) {
        return jdbcAdminService.execute(query);
    }

    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @PostMapping("/paginate/orient")
    public PaginatedDto<SqlResult> executeOrient(@RequestBody PaginationRequestWrapperDto<String> paginationRequestWrapperDto) {
        return orientAdminService.paginate(paginationRequestWrapperDto);
    }

    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @PostMapping("/paginate/jdbc")
    public PaginatedDto<SqlResult> executeh2(@RequestBody PaginationRequestWrapperDto<String> paginationRequestWrapperDto) {
        return jdbcAdminService.paginate(paginationRequestWrapperDto);
    }
}
