module simple_dual_one_clock #(
    parameter DATA_WIDTH = 8,
    parameter ADDR_WIDTH = 4
) (
    clk,
    readEnable,
    writeEnable,
    readAddress,
    writeAddress,
    readData,
    writeData
);

  input clk;
  input readEnable;
  input [(ADDR_WIDTH-1):0] readAddress;
  output [(DATA_WIDTH-1):0] readData;
  input writeEnable;
  input [(ADDR_WIDTH-1):0] writeAddress;
  input [(DATA_WIDTH-1):0] writeData;

  reg [(DATA_WIDTH-1):0] ram[2**ADDR_WIDTH];

  always @(posedge clk) begin
    if (writeEnable) ram[writeAddress] <= writeData;
  end

  assign readData = ram[readAddress];

endmodule
